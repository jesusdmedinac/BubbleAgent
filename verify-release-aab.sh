#!/usr/bin/env bash
set -e

AAB_PATH="androidApp/build/outputs/bundle/release/androidApp-release.aab"
EXPECTED_PACKAGE="com.jesusdmedinac.bubble"
EXPECTED_VERSION_CODE="20"
EXPECTED_VERSION_NAME="1.2.1"
EXPECTED_TARGET_SDK="36"
EXPECTED_MIN_SDK="26"
EXPECTED_SHA256="77:CF:7E:2F:BD:69:CB:E7:F2:03:D1:88:B4:92:3D:5B:39:D8:D6:E6:15:F3:60:78:1D:38:8A:59:C1:FC:C0:5C"

echo "=========================================="
echo " Validando Android App Bundle (AAB) Release"
echo "=========================================="

if [ ! -f "$AAB_PATH" ]; then
    echo "❌ Error: No se encontró el archivo AAB en $AAB_PATH"
    echo "   Ejecuta: ./gradlew :androidApp:bundleRelease"
    exit 1
fi

AAB_SIZE=$(ls -lh "$AAB_PATH" | awk '{print $5}')
AAB_DATE=$(ls -lh "$AAB_PATH" | awk '{print $6, $7, $8}')
echo "✅ [1/6] Archivo AAB encontrado ($AAB_SIZE, compilado: $AAB_DATE)"

# Inspección de AndroidManifest.xml (Proto XML) dentro de base/manifest/
PARSED_DATA=$(python3 - <<EOF
import zipfile, sys

try:
    with zipfile.ZipFile("$AAB_PATH", "r") as z:
        manifest = z.read("base/manifest/AndroidManifest.xml")
        
        def find_attr(attr_name):
            pos = manifest.find(attr_name.encode("utf-8"))
            if pos == -1:
                return None
            sub = manifest[pos:pos+60]
            import re
            strings = re.findall(rb'[a-zA-Z0-9_\.\-]+', sub)
            for s in strings[1:]:
                val = s.decode("utf-8")
                if val != attr_name:
                    return val
            return None

        pkg = find_attr("package")
        vc = find_attr("versionCode")
        vn = find_attr("versionName")
        target_sdk = find_attr("targetSdkVersion")
        min_sdk = find_attr("minSdkVersion")

        print(f"PACKAGE={pkg or ''}")
        print(f"VERSION_CODE={vc or ''}")
        print(f"VERSION_NAME={vn or ''}")
        print(f"TARGET_SDK={target_sdk or ''}")
        print(f"MIN_SDK={min_sdk or ''}")
except Exception as e:
    print(f"ERROR={e}", file=sys.stderr)
    sys.exit(1)
EOF
)

eval "$PARSED_DATA"

# 2. Package Name
if [ "$PACKAGE" = "$EXPECTED_PACKAGE" ]; then
    echo "✅ [2/6] Package Name correcto: $PACKAGE"
else
    echo "❌ [2/6] Package Name esperado '$EXPECTED_PACKAGE' pero encontrado '$PACKAGE'"
    exit 1
fi

# 3. Target SDK (Android 16 / API 36)
if [ "$TARGET_SDK" = "$EXPECTED_TARGET_SDK" ]; then
    echo "✅ [3/6] Target SDK coincide: $TARGET_SDK (Android 16)"
else
    echo "❌ [3/6] Target SDK esperado '$EXPECTED_TARGET_SDK' pero encontrado '$TARGET_SDK'"
    exit 1
fi

# 4. Version Code & Name
if [ "$VERSION_CODE" = "$EXPECTED_VERSION_CODE" ] && [ "$VERSION_NAME" = "$EXPECTED_VERSION_NAME" ]; then
    echo "✅ [4/6] Versión correcta: versionCode=$VERSION_CODE, versionName=$VERSION_NAME"
else
    echo "❌ [4/6] Versión esperada code=$EXPECTED_VERSION_CODE name=$EXPECTED_VERSION_NAME (encontrado code=$VERSION_CODE name=$VERSION_NAME)"
    exit 1
fi

# 5. Min SDK
if [ "$MIN_SDK" = "$EXPECTED_MIN_SDK" ]; then
    echo "✅ [5/6] Min SDK verificado: $MIN_SDK"
else
    echo "⚠️  [5/6] Min SDK detectado: $MIN_SDK (esperado: $EXPECTED_MIN_SDK)"
fi

# 6. Certificado y Firma criptográfica
CERT_INFO=$(keytool -printcert -jarfile "$AAB_PATH" 2>/dev/null || true)
if echo "$CERT_INFO" | grep -q "$EXPECTED_SHA256"; then
    echo "✅ [6/6] Firma criptográfica verificada (SHA-256 coincide con el keystore de release):"
    echo "       $EXPECTED_SHA256"
else
    echo "❌ [6/6] Firma criptográfica no coincide con el keystore esperado!"
    echo "$CERT_INFO" | grep "SHA256:" || echo "Sin información de firma."
    exit 1
fi

echo "=========================================="
echo " 🎉 ¡EL BUNDLE AAB ESTÁ LISTO PARA GOOGLE PLAY! "
echo "=========================================="
