#!/usr/bin/env python3
"""Build 18 independently installable resource entry mods, each nesting the same runtime."""
from pathlib import Path
import json,zipfile,hashlib,struct,zlib,sys
ROOT=Path(__file__).resolve().parent
SPECS=json.loads((ROOT/'specs.json').read_text())
OUT=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'dist'
OUT.mkdir(parents=True,exist_ok=True)
core=ROOT/'build/libs/pvp-tools-core-1.0.0.jar'
if not core.exists():raise SystemExit('Run gradle build first.')
glyphs={
'A':['01110','10001','10001','11111','10001','10001','10001'],
'C':['01111','10000','10000','10000','10000','10000','01111'],
'D':['11110','10001','10001','10001','10001','10001','11110'],
'E':['11111','10000','10000','11110','10000','10000','11111'],
'F':['11111','10000','10000','11110','10000','10000','10000'],
'H':['10001','10001','10001','11111','10001','10001','10001'],
'I':['11111','00100','00100','00100','00100','00100','11111'],
'K':['10001','10010','10100','11000','10100','10010','10001'],
'L':['10000','10000','10000','10000','10000','10000','11111'],
'M':['10001','11011','10101','10101','10001','10001','10001'],
'N':['10001','11001','10101','10011','10001','10001','10001'],
'O':['01110','10001','10001','10001','10001','10001','01110'],
'P':['11110','10001','10001','11110','10000','10000','10000'],
'R':['11110','10001','10001','11110','10100','10010','10001'],
'S':['01111','10000','10000','01110','00001','00001','11110'],
'T':['11111','00100','00100','00100','00100','00100','00100']}
def png(text):
 pixels=[[(23,27,34,255) for _ in range(64)]for _ in range(64)]
 for y in range(4,60):
  for x in range(4,60):
   if y<7 or y>=57 or x<7 or x>=57:pixels[y][x]=(120,220,255,255)
 for i,ch in enumerate(text[:2]):
  for gy,row in enumerate(glyphs[ch]):
   for gx,b in enumerate(row):
    if b=='1':
     for dy in range(3):
      for dx in range(3):pixels[21+gy*3+dy][15+i*19+gx*3+dx]=(235,244,255,255)
 raw=b''.join(b'\0'+bytes(v for px in row for v in px)for row in pixels)
 def chunk(kind,data):return struct.pack('!I',len(data))+kind+data+struct.pack('!I',zlib.crc32(kind+data)&0xffffffff)
 return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',64,64,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')
manifest=[]
for id,title,desc,threshold,icon in SPECS:
 cls='Menu'+''.join(x.title()for x in id.split('_'))
 metadata={'schemaVersion':1,'id':id,'version':'1.0.0+1.21.11','name':title,'description':desc,'authors':['Vaileks'],'license':'MIT','environment':'client','icon':f'assets/{id}/icon.png','entrypoints':{'modmenu':[f'dev.vaileks.pvp.{cls}']},'jars':[{'file':'META-INF/jars/pvp-tools-core-1.0.0.jar'}],'depends':{'fabricloader':'>=0.19.2','minecraft':'1.21.11','java':'>=21','vaileks_pvp_core':'>=1.0.0'},'suggests':{'modmenu':'*'}}
 initials=''.join(x[0]for x in title.split())[:2].upper()
 path=OUT/f'{id.replace("_","-")}-1.0.0+1.21.11.jar'
 with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED)as z:
  z.writestr('fabric.mod.json',json.dumps(metadata,ensure_ascii=False,indent=2))
  z.write(core,'META-INF/jars/pvp-tools-core-1.0.0.jar')
  z.writestr(f'assets/{id}/icon.png',png(initials))
  z.writestr('LICENSE',(ROOT/'LICENSE').read_bytes())
 manifest.append({'id':id,'name':title,'file':path.name,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
(OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2))
print(f'Packaged {len(manifest)} standalone JARs in {OUT}')
