"""Βάζει το εικονίδιο The Pi Helper στο Android project (μετά το `npx cap add android`)."""
import pathlib
from PIL import Image, ImageDraw

BG = (189, 228, 209)
src = Image.open('resources/icon-1024.png').convert('RGB')
res = pathlib.Path('android/app/src/main/res')
dens = {'mdpi': (48, 108), 'hdpi': (72, 162), 'xhdpi': (96, 216), 'xxhdpi': (144, 324), 'xxxhdpi': (192, 432)}

for d, (legacy, fg) in dens.items():
    out = res / f'mipmap-{d}'
    out.mkdir(parents=True, exist_ok=True)
    sq = src.resize((legacy, legacy), Image.LANCZOS)
    sq.save(out / 'ic_launcher.png')
    mask = Image.new('L', (legacy * 4, legacy * 4), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, legacy * 4 - 1, legacy * 4 - 1), fill=255)
    mask = mask.resize((legacy, legacy), Image.LANCZOS)
    rd = Image.new('RGBA', (legacy, legacy), (0, 0, 0, 0)); rd.paste(sq, (0, 0), mask)
    rd.save(out / 'ic_launcher_round.png')
    canvas = Image.new('RGB', (fg, fg), BG)
    s = int(fg * 0.62)
    canvas.paste(src.resize((s, s), Image.LANCZOS), ((fg - s) // 2, (fg - s) // 2))
    canvas.save(out / 'ic_launcher_foreground.png')

vals = res / 'values'
vals.mkdir(parents=True, exist_ok=True)
(vals / 'ic_launcher_background.xml').write_text(
    '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <color name="ic_launcher_background">#BDE4D1</color>\n</resources>\n', encoding='utf8')
print('εικονίδια εγκαταστάθηκαν')
