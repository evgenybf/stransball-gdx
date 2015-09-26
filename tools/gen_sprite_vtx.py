"""Generate vertex file for title.png

The goal is to avoid pixel matxhing at all.

Note, that concave polyons can be decomposed into convex polygons with
EarClippingTriangulator
https://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/EarClippingTriangulator.html
or
GeometryUtils.decomposeIntoConvex(vtx)
See: https://bitbucket.org/dermetfan/libgdx-utils

"""
import re
# import numpy as np
import cv2

DEBUG_OUT = 1

TILES_PACK = "../core/assets/graphics/tiles.pack"
TILES_PNG = "../core/assets/graphics/tiles-mask.png"
VTX_FILE = "tiles.vtx"

VTX_TEMPLATE = """\
%s
  vertices: %s
  size: %d, %d
  index: %d    	
"""

# Sample input file:
# tiles.png
# format: RGBA8888
# filter: Linear,Linear
# repeat: none
# ship
#  rotate: false
#  xy: 32, 240
#  size: 32, 32
#  orig: 32, 32
#  offset: 0, 0
#  index: -1
# ...


class Obj:

    def __init__(self, name):
        self.name = name
        self.x = None
        self.y = None
        self.size_x = None
        self.size_y = None
        self.index = None

    def get_full_name(self):
        if self.index is None:
            return self.name
        else:
            return "%s_%03d" % (self.name, self.index)

    def is_empty(self):
        return self.x is None and self.y is None and self.size_x is None and self.size_y is None

    def __str__(self):
        return "%s: %s:%s-%s:%s" % (self.get_full_name(), self.x, self.y, self.size_x, self.size_y)


RE_ATTR_NAME_VALUE = re.compile(r"\s+([A-Za-z]+):\s*([0-9, -]+)")


def split_xy(s):
    return [int(x) for x in s.split(",")]


def read_pack(filename):
    with file(filename, "r") as fi:
        fi.readline()
        obj = None
        while True:
            line = fi.readline()
            if not line:
                break
            line = line.rstrip()
            if not line:
                continue
            if line[0] not in (' ', '\t'):
                if obj is not None and not obj.is_empty():
                    yield obj
                obj = Obj(line)
            else:
                m = RE_ATTR_NAME_VALUE.match(line)
                if m:
                    name, value = m.groups((1, 2,))
                    if name == "xy":
                        obj.x, obj.y = split_xy(value)
                    elif name == "size":
                        obj.size_y, obj.size_x = split_xy(value)
                    elif name == "index":
                        index = int(value.strip())
                        if index >= 0:
                            obj.index = index

        if obj is not None and not obj.is_empty():
            yield obj


def write_vec(out, obj, contours):
    index = (obj.index if obj.index is not None else -1)
    if contours is not None:
        vertices = ",".join([str(x if i%2==0 else -x) for i, x in enumerate(contours.flatten())])
    else:
        vertices = ""
    out.write(VTX_TEMPLATE % (obj.name, vertices, obj.size_x, obj.size_y, index))


def main():
    im = cv2.imread(TILES_PNG)

    with file(VTX_FILE, "w") as fo:
        for obj in read_pack(TILES_PACK):
            print (obj)

            sim = im[obj.y:obj.y + obj.size_y, obj.x:obj.x + obj.size_x]
            imgray = cv2.cvtColor(sim, cv2.COLOR_BGR2GRAY)
            ret, thresh = cv2.threshold(imgray, 127, 255, 2)

            contours, hierarchy = cv2.findContours(
                thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_NONE)
            colors = ((0, 255, 0), (0, 0, 255), (255, 0, 0),
                      (0, 255, 255), (255, 255, 0), (255, 0, 255))

            if len(contours) > 0:
                max_vtx = contours[0]
                for vtx in contours[1:]:
                    if len(max_vtx) < len(vtx):
                        max_vtx = vtx
                epsilon = 0.01 * cv2.arcLength(max_vtx, True)
                approx = cv2.approxPolyDP(max_vtx, epsilon, True)
                cv2.drawContours(sim, approx, -1, colors[0], 1)

                if DEBUG_OUT:
                    import os.path
                    if not os.path.exists("output"):
                        os.mkdir("output")
                    cv2.imwrite("output/%s.png" % (obj.get_full_name(),), sim)

                write_vec(fo, obj, max_vtx)
            else:
                print ("...skipped")
                write_vec(fo, obj, None)


if __name__ == "__main__":
    main()
