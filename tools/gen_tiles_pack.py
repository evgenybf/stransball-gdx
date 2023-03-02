TILES_PACK = "tiles.pack.part"

TITLE_TEMPLATE = """\
tile
  rotate: false
  xy: %d, %d
  size: 16, 16
  orig: 16, 16
  offset: 0, 0
  index: %d
"""


def main():
    with file(TILES_PACK, "w") as fo:
        for i in xrange(500):
            x = (i % 20) * 16
            y = (i / 20) * 16
            fo.write(TITLE_TEMPLATE % (x, y, i))


if __name__ == "__main__":
    main()
