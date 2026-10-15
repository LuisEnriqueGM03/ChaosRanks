from pathlib import Path
import json, tomllib, zipfile, hashlib
ROOT=Path(__file__).resolve().parent.parent
records=[]
for loader in ('fabric','forge','neoforge'):
 jar=ROOT/loader/'build/libs'/f'chaosranks-{loader}-26.3-1.0.2.jar'
 with zipfile.ZipFile(jar) as z:
  if loader=='fabric':
   m=json.loads(z.read('fabric.mod.json')); assert m['version']=='1.0.2'; assert m['authors']==['Goddark83'] and m['depends']['minecraft']=='~26.3' and m['depends']['java']=='>=25'
  else:
   path='META-INF/neoforge.mods.toml' if loader=='neoforge' else 'META-INF/mods.toml'
   m=tomllib.loads(z.read(path).decode()); assert m['mods'][0]['version']=='1.0.2'; assert m['mods'][0]['authors']=='Goddark83'
   assert next(d for d in m['dependencies']['chaosranks'] if d['modId']=='minecraft')['versionRange']=='[26.3,26.3.1)'
  assert int.from_bytes(z.read('com/chaos/ranks/ChaosRanks.class')[6:8],'big')==69
  assert z.read('META-INF/services/com.chaos.ranks.platform.Platform').decode().strip()==f'com.chaos.ranks.{loader}.{loader.title()}Platform'
  assert json.loads(z.read('pack.mcmeta'))['pack']['min_format']==[97,1]
  assert json.loads(z.read('pack.mcmeta'))['pack']['max_format']==[121,0]
  assert len(json.loads(z.read('assets/chaosranks/badge_meta.json'))['icons'])==144
  assert 'LICENSE' in z.namelist()
  assert not any(n.startswith(f'com/chaos/ranks/{other}/') for other in ('fabric','forge','neoforge') if other!=loader for n in z.namelist())
 records.append(dict(loader=loader,minecraft='26.3',version='1.0.2',author='Goddark83',file=jar.name,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),branch=f'{loader}-26.3'))
 print(loader+': OK')
(ROOT/'release-artifacts-26.3.json').write_text(json.dumps(records,indent=2)+'\n')
