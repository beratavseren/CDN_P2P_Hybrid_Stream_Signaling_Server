pipeline {
    agent any

    environment {
        // Jenkins sunucusundaki SourceMeter yolu. Kendi sistemine göre (Windows/Linux) güncelle.
        SOURCEMETER_PATH = "/opt/SourceMeter/Java/SourceMeterJava"

        // Asıl beynimizin (FastAPI) çalıştığı adres
        API_URL = "http://localhost:8000/api/v1/predict"
    }

    stages {
        stage('SourceMeter Statik Analiz') {
            steps {
                echo "🔍 1. Adım: SourceMeter ile proje taranıyor ve metrikler çıkarılıyor..."
                // Not: Eğer Jenkins'i Windows'ta kurduysan buradaki 'sh' komutlarını 'bat' olarak değiştir.
                sh "${SOURCEMETER_PATH} -projectBaseDir . -resultsDir ./sm_results -projectName CodeSmellProject"
            }
        }

        stage('Yapay Zeka (GraphCodeBERT) Kod Kokusu Kontrolü') {
            steps {
                script {
                    echo "🚀 2. Adım: Yapay Zeka denetimi başlatılıyor..."

                    // Son commit ile sadece değişen .java dosyalarını bul
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD | grep '\\.java\$' || true", returnStdout: true).trim()

                    if (changedFiles.isEmpty()) {
                        echo "✅ Değişen Java dosyası bulunamadı. Yapay Zeka kontrolü atlanıyor."
                    } else {
                        def files = changedFiles.split('\n')
                        for (file in files) {
                            echo "--------------------------------------------------------"
                            echo "⚙️ İşlenen Dosya: ${file}"

                            // Dosya yolundan sadece sınıf adını çıkar (Örn: src/AppenderTable.java -> AppenderTable)
                            def className = file.tokenize('/').last().replaceAll('\\.java$', '')

                            // 1. Yazdığımız Python betiğini (İsviçre Çakısını) çalıştır ve payload.json üret
                            sh "python MetrikAdapter.py --java \"${file}\" --csv \"./sm_results/CodeSmellProject/java/Class.csv\" --class_name \"${className}\""

                            // 2. Üretilen JSON dosyasını API'ye (FastAPI) fırlat ve cevabı al
                            def response = sh(script: "curl -s -X POST \"${API_URL}\" -H \"Content-Type: application/json\" -d @payload.json", returnStdout: true).trim()

                            // 3. API'den dönen JSON sonucunu Jenkins içinde çözümle
                            def jsonSlurper = new groovy.json.JsonSlurper()
                            def result = jsonSlurper.parseText(response)

                            // 4. KARAR (GATEKEEPER) ANI!
                            if (result.status == "fail") {
                                echo "🚨 YAPAY ZEKA REDDETTİ! KOD KOKUSU TESPİT EDİLDİ 🚨"
                                echo "❌ Sınıf: ${className}"
                                echo "🦠 Tespit Edilen Kusurlar: ${result.details}"

                                // error() komutu Jenkins boru hattını (pipeline) KIRMIZIYA düşürür ve build'i iptal eder.
                                error("⛔ Pipeline kırıldı! Lütfen koddaki kokuları refactor edip tekrar pushlayın.")
                            } else {
                                echo "✅ Yapay Zeka Onayı: ${className} kod yapısı temiz. Geçişe izin verildi."
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "🧹 3. Adım: Geçici dosyalar temizleniyor..."
            // Güvenlik ve yer tasarrufu için analiz sonrası artıkları sil
            sh "rm -rf ./sm_results payload.json || true"
        }
    }
}