"""
Decode lp file.

Usage:
decode_lp.py [fileIn [fileOut]]

"""
import sys


def openFile(fileName, params, default):
    if fileName:
        return file(fileName, params)
    else:
        return default


def main(argv):
    inFileName = None
    outFileName = None

    if len(argv) > 1:
        inFileName = argv[1]
    if len(argv) > 2:
        outFileName = argv[2]

    print("Decoding %s -> %s" % (inFileName, outFileName))

    seed = (1, 7, 3, 9, 34, 1, 24, 1, 8, 25, 14, 65, 2, 7, 4, 1)
    i = 0

    with openFile(inFileName, "rb", sys.stdin) as fi, openFile(
        outFileName, "wb", sys.stdout
    ) as fo:
        while True:
            val1 = fi.read(1)
            if not val1:
                break

            val2 = fi.read(1)
            if not val2:
                print("warning: unexpected EOF")
                break

            val = (
                ((ord(val1) - ord("A")) << 4) + ord(val2) - ord("A") - seed[(i % 16)]
            ) % 256

            fo.write(chr(val))
            i += 1


if __name__ == "__main__":
    main(sys.argv[:])
