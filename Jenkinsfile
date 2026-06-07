pipeline {
    agent any

    environment {
        // DİKKAT: SourceMeter'ın Windows'taki tam .exe yolunu buraya yazmalısın.
        // Örnek: "C:\\Program Files\\SourceMeter\\Java\\SourceMeterJava.exe"
        SOURCEMETER_PATH = "C:/Users/musab/Downloads/SourceMeter-10.2.0-x64-Windows/SourceMeter-10.2.0-x64-Windows/Java/AnalyzerJava.exe"
        PYTHON_PATH = "C:/Users/musab/AppData/Local/Programs/Python/Python312/python.exe"
        API_URL = "http://localhost:8000/api/v1/predict"
        PYTHONIOENCODING = "utf-8"
    }

    stages {
            stage('SourceMeter Statik Analiz') {
                steps {
                    echo "🔍 1. Adım: SourceMeter ile proje taranıyor..."
                    powershell "& '${SOURCEMETER_PATH}' -projectBaseDir:${env.WORKSPACE} -resultsDir:${env.WORKSPACE}\\sm_results -projectName:CodeSmellProject"
                }
            }

            stage('Yapay Zeka (GraphCodeBERT) Kod Kokusu Kontrolü') {
                steps {
                    script {
                        echo "🚀 2. Adım: Yapay Zeka denetimi başlatılıyor..."

                        def diffOutput = powershell(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                        def changedFiles = diffOutput.split('\r?\n').findAll { it.trim().endsWith('.java') }

                        if (changedFiles.isEmpty()) {
                            echo "✅ Değişen Java dosyası bulunamadı. Yapay Zeka kontrolü atlanıyor."
                        } else {
                            // YENİ: Tarihli klasörü atlamak için Class.csv dosyasının yerini dinamik olarak arayıp buluyoruz
                            def csvFullPath = powershell(script: "(Get-ChildItem -Path sm_results -Filter '*Class.csv' -Recurse | Select-Object -First 1).FullName", returnStdout: true).trim()

                            if (!csvFullPath) {
                                error("⛔ Class.csv dosyası bulunamadı! SourceMeter analizi başarısız olmuş olabilir.")
                            }

                            for (file in changedFiles) {
                                file = file.trim()
                                echo "--------------------------------------------------------"
                                echo "⚙️ İşlenen Dosya: ${file}"

                                def className = file.tokenize('/').last().replaceAll('\\.java$', '')

                                // YENİ: Bulunan o dinamik CSV yolunu Python'a veriyoruz
                                powershell "& '${PYTHON_PATH}' MetrikAdapter.py --java \"${file}\" --csv \"${csvFullPath}\" --class_name \"${className}\""

                                def response = powershell(script: "(Invoke-RestMethod -Uri '${API_URL}' -Method Post -ContentType 'application/json' -InFile 'payload.json' | ConvertTo-Json -Depth 10 -Compress)", returnStdout: true).trim()

                                def jsonSlurper = new groovy.json.JsonSlurper()
                                def result = jsonSlurper.parseText(response)

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
                powershell "if (Test-Path sm_results) { Remove-Item -Recurse -Force sm_results }; if (Test-Path payload.json) { Remove-Item payload.json }"
            }
        }
    }