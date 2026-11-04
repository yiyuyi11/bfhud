from pathlib import Path
import os, subprocess, zipfile, tomllib, shutil

ROOT = Path(__file__).resolve().parent
JAVA = Path('C:/Program Files/Java/jdk-21/bin')

metadata = (ROOT / 'src/main/resources/META-INF/neoforge.mods.toml').read_bytes()
if metadata.startswith(b'\xef\xbb\xbf'):
    raise SystemExit('Invalid NeoForge metadata: remove UTF-8 BOM')
tomllib.loads(metadata.decode('utf-8'))

# Classpath of the NeoForge 21.1 dev environment. Set BF_CLASSPATH_FILE to point at a file that holds the
# joined classpath (one '-classpath' entry per line separated by ';'), or place this repository next to a
# BattlefieldOps checkout that produced build/compile-classpath.txt.
cp_file = Path(os.environ.get('BF_CLASSPATH_FILE', ROOT.parent / 'BattlefieldOps/build/compile-classpath.txt'))
if not cp_file.is_file():
    raise SystemExit(
        'Missing dependency classpath: ' + str(cp_file) +
        '\nGenerate it from a NeoForge workspace (see README) or set BF_CLASSPATH_FILE.'
    )
cp = [p for p in cp_file.read_text('utf-8-sig').strip().split(';') if Path(p).is_file()]
if not cp:
    raise SystemExit('Dependency classpath is empty: ' + str(cp_file))

classes = ROOT / 'build/classes'
if classes.exists():
    shutil.rmtree(classes)
classes.mkdir(parents=True, exist_ok=True)
sources = list((ROOT / 'src/main/java').rglob('*.java'))
args = ['--release', '21', '-encoding', 'UTF-8', '-proc:none', '-classpath', ';'.join(cp), '-d', str(classes)] + list(map(str, sources))
(ROOT / 'build').mkdir(exist_ok=True)
(ROOT / 'build/javac.args').write_text('\n'.join('"' + s.replace('\\', '/') + '"' for s in args), 'utf-8')
result = subprocess.run([str(JAVA / 'javac.exe'), '@' + str(ROOT / 'build/javac.args')], cwd=ROOT)
if result.returncode:
    raise SystemExit(result.returncode)

out = ROOT / 'build/libs/modernhud-full-1.0.0-neoforge-1.21.1.jar'
out.parent.mkdir(exist_ok=True)
with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as z:
    for folder in [classes, ROOT / 'src/main/resources']:
        for f in folder.rglob('*'):
            if f.is_file():
                z.write(f, f.relative_to(folder).as_posix())
print('BUILD OK:', out)

