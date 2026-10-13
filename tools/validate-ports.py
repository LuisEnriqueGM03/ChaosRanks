"""Validate the final remapped Minecraft 1.20.1 jars."""
from pathlib import Path
import hashlib, json, tomllib, zipfile
ROOT = Path(__file__).resolve().parent.parent
records=[]
for loader in ('fabric','forge','neoforge'):
 jar=ROOT/loader/'build/libs'/f'chaosranks-{loader}-1.20.1-1.0.1.jar'
 with zipfile.ZipFile(jar) as z:
  if loader=='fabric':
   m=json.loads(z.read('fabric.mod.json'))
   assert m['authors']==['Goddark83'] and m['version']=='1.0.1'
   assert m['depends']['minecraft']=='~1.20.1' and m['depends']['java']=='>=17'
  else:
   m=tomllib.loads(z.read('META-INF/mods.toml').decode())
   assert m['mods'][0]['authors']=='Goddark83' and m['mods'][0]['version']=='1.0.1'
   deps={d['modId']:d for d in m['dependencies']['chaosranks']}
   assert deps['minecraft']['versionRange']=='[1.20.1,1.20.2)'
   assert deps['forge']['versionRange'].startswith('[47.')
  assert int.from_bytes(z.read('com/chaos/ranks/ChaosRanks.class')[6:8],'big')==61
  assert z.read('META-INF/services/com.chaos.ranks.platform.Platform').decode().strip()==f'com.chaos.ranks.{loader}.{loader.title()}Platform'
  mixins=json.loads(z.read('chaosranks.mixins.json'))
  assert mixins['compatibilityLevel']=='JAVA_17'
  refmap=json.loads(z.read(mixins['refmap']))
  assert len(refmap['mappings'])==5
  assert len(json.loads(z.read('assets/chaosranks/badge_meta.json'))['icons'])==144
  assert json.loads(z.read('pack.mcmeta'))['pack']['pack_format']==15
  assert 'LICENSE' in z.namelist()
  assert not any(n.startswith(f'com/chaos/ranks/{other}/') for other in ('fabric','forge','neoforge') if other!=loader for n in z.namelist())
 records.append(dict(loader=loader,minecraft='1.20.1',version='1.0.1',author='Goddark83',file=jar.name,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),branch=f'{loader}-1.20.1'))
 print(loader+': OK')
(ROOT/'release-artifacts-1.20.1.json').write_text(json.dumps(records,indent=2)+'\n')
