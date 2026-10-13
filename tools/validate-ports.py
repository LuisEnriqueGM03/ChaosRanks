"""Inspect the two release jars without loading Minecraft or any account credentials."""
from pathlib import Path
import hashlib
import json
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parent.parent
for loader, minimum in (("neoforge", "21.4.158"), ("forge", "54.1.14")):
    jar = ROOT / loader / "build/libs" / f"chaosranks-{loader}-1.21.4-1.0.1.jar"
    with zipfile.ZipFile(jar) as contents:
        descriptor_path = "META-INF/neoforge.mods.toml" if loader == "neoforge" else "META-INF/mods.toml"
        descriptor = tomllib.loads(contents.read(descriptor_path).decode())
        mod = descriptor["mods"][0]
        assert mod["modId"] == "chaosranks" and mod["version"] == "1.0.1"
        assert mod["authors"] == "Goddark83"
        dependencies = {item["modId"]: item for item in descriptor["dependencies"]["chaosranks"]}
        assert dependencies["minecraft"]["versionRange"] == "[1.21.4,1.21.5)"
        assert dependencies[loader]["versionRange"].startswith("[" + minimum + ",")
        assert int.from_bytes(contents.read("com/chaos/ranks/ChaosRanks.class")[6:8], "big") == 65
        provider = contents.read("META-INF/services/com.chaos.ranks.platform.Platform").decode().strip()
        assert provider == f"com.chaos.ranks.{loader}.{loader.title()}Platform"
        assert "fabric.mod.json" not in contents.namelist()
        assert not any(name.startswith("com/chaos/ranks/" + other + "/") for other in ("fabric", "neoforge", "forge") if other != loader for name in contents.namelist())
        assert "LICENSE" in contents.namelist()
        mixins = json.loads(contents.read("chaosranks.mixins.json"))
        assert mixins["required"] and mixins["compatibilityLevel"] == "JAVA_21"
        assert len(mixins["mixins"]) == 3 and len(mixins["client"]) == 2
        assert len(json.loads(contents.read("assets/chaosranks/badge_meta.json"))["icons"]) == 144
        assert "data/chaosranks/chat_type/rank.json" in contents.namelist()
    print(json.dumps({"loader": loader, "file": jar.name, "sha256": hashlib.sha256(jar.read_bytes()).hexdigest(), "validation": "OK"}))

jar = ROOT / 'fabric/build/libs/chaosranks-fabric-1.21.4-1.0.1.jar'
with zipfile.ZipFile(jar) as z:
    m = json.loads(z.read('fabric.mod.json'))
    assert m['authors'] == ['Goddark83'] and m['version'] == '1.0.1'
    assert m['depends']['minecraft'] == '~1.21.4'
    assert int.from_bytes(z.read('com/chaos/ranks/ChaosRanks.class')[6:8], 'big') == 65
    assert z.read('META-INF/services/com.chaos.ranks.platform.Platform').decode().strip() == 'com.chaos.ranks.fabric.FabricPlatform'
print('fabric: OK')
records=[]
for loader in ('fabric','forge','neoforge'):
    jar=ROOT/loader/'build/libs'/f'chaosranks-{loader}-1.21.4-1.0.1.jar'
    with zipfile.ZipFile(jar) as z:
        assert json.loads(z.read('pack.mcmeta'))['pack']['pack_format']==46
        assert 'LICENSE' in z.namelist()
    records.append(dict(loader=loader,minecraft='1.21.4',version='1.0.1',author='Goddark83',file=jar.name,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),branch=f'{loader}-1.21.4'))
(ROOT/'release-artifacts-1.21.4.json').write_text(json.dumps(records,indent=2)+'\n')
