pipeline {
    agent any

    environment {
        // DİKKAT: SourceMeter'ın Windows'taki tam .exe yolunu buraya yazmalısın.
        // Örnek: "C:\\Program Files\\SourceMeter\\Java\\SourceMeterJava.exe"
        SOURCEMETER_PATH = "C:/Users/musab/Downloads/SourceMeter-10.2.0-x64-Windows/SourceMeter-10.2.0-x64-Windows/Java/AnalyzerJava.exe"

        API_URL = "http://localhost:8000/api/v1/predict"
    }

    stages {
        stage('SourceMeter Statik Analiz') {
            steps {
                echo "🔍 1. Adım: SourceMeter ile proje taranıyor..."
                powershell "& '${SOURCEMETER_PATH}' -projectBaseDir=. -resultsDir=sm_results -projectName=CodeSmellProject"
            }
        }

        stage('Yapay Zeka (GraphCodeBERT) Kod Kokusu Kontrolü') {
            steps {
                script {
                    echo "🚀 2. Adım: Yapay Zeka denetimi başlatılıyor..."

                    // Sadece değişen Java dosyalarını bul (PowerShell uyumlu)
                    def changedFiles = powershell(script: "(git diff --name-only HEAD~1 HEAD) -match '\\.java\$'", returnStdout: true).trim()

                    if (changedFiles == null || changedFiles.isEmpty()) {
                        echo "✅ Değişen Java dosyası bulunamadı. Yapay Zeka kontrolü atlanıyor."
                    } else {
                        def files = changedFiles.split('\r?\n')
                        for (file in files) {
                            file = file.trim()
                            if (file.isEmpty()) continue

                            echo "--------------------------------------------------------"
                            echo "⚙️ İşlenen Dosya: ${file}"

                            def className = file.tokenize('/').last().replaceAll('\\.java$', '')

                            // 1. Adapter'ı çalıştır
                            powershell "python MetrikAdapter.py --java \"${file}\" --csv \"./sm_results/CodeSmellProject/java/Class.csv\" --class_name \"${className}\""

                            // 2. PowerShell Invoke-RestMethod ile API'ye gönder
                            def response = powershell(script: "(Invoke-RestMethod -Uri '${API_URL}' -Method Post -ContentType 'application/json' -InFile 'payload.json' | ConvertTo-Json -Depth 10 -Compress)", returnStdout: true).trim()

                            def jsonSlurper = new groovy.json.JsonSlurper()
                            def result = jsonSlurper.parseText(response)

                            // 3. Karar Anı
                            if (result.status == "fail") {
                                echo "🚨 YAPAY ZEKA REDDETTİ! KOD KOKUSU TESPİT EDİLDİ 🚨"
                                echo "❌ Sınıf: ${className}"
                                echo "🦠 Kusurlar: ${result.details}"
                                error("⛔ Pipeline kırıldı! Lütfen koddaki kokuları refactor edip tekrar pushlayın.")
                            } else {
                                echo "✅ Yapay Zeka Onayı: ${className} kod yapısı temiz."
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
            // PowerShell uyumlu temizlik komutu (rm -rf yerine)
            powershell "if (Test-Path sm_results) { Remove-Item -Recurse -Force sm_results }; if (Test-Path payload.json) { Remove-Item payload.json }"
        }
    }
}