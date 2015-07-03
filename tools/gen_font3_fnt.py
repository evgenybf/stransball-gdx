"""Generates libgdx fnt file for using font3.png
"""
import sys

FNT_FILE_NAME = "font3.fnt"

HEADER_STR = """\
info face="font3.png" size=0 bold=0 italic=0 charset="" unicode=0 stretchH=0 smooth=0 aa=0 padding=0,0,0,0 spacing=1,1
common lineHeight=10 base=8 scaleW=0 scaleH=0 pages=1 packed=0
page id=0 file="font3.png"
"""

CHARSCOUNT_STR = """\
chars count=%d
"""

CHARID_STR = """\
char id=%d   x=%d    y=%d     width=6    height=8 xoffset=0     yoffset=0     xadvance=6    page=0  chnl=0
"""

# character layout in font3.png
#________________________________ !"#$%'_____,
#-./0123456789:____? ABCDEFGHIJKLMNOPQRSTUVWXY
#Z[\]__`abcdefghijklmnopqrstuvwxyz{|}

def main():
    with file(FNT_FILE_NAME, "w") as fi:
        write = fi.write
        write(HEADER_STR)
        count = 125 + 1 - 32
        write(CHARSCOUNT_STR % (count,))
        for ch in xrange(32, 125+1):
            x = ch %45 * 7 + 1
            y = ch // 45 * 9 + 1
            write(CHARID_STR % (ch, x, y))


if __name__ == '__main__':
    main()
