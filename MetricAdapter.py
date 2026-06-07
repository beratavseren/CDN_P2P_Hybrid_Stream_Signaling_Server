import pandas as pd
import javalang
import json
import argparse
import math
import sys

# Modelin BEKLEDİĞİ %100 KESİN 81 metrik listesi
EGITIM_METRIK_SIRASI = [
    'LOC', 'NOPA', 'AvgCyclomatic', 'AvgCyclomaticModified', 'AvgCyclomaticStrict', 'AvgEssential',
    'AvgLine', 'AvgLineBlank', 'AvgLineCode', 'AvgLineComment', 'CountClassBase', 'CountClassCoupled',
    'CountClassDerived', 'CountDeclClassMethod', 'CountDeclClassVariable', 'CountDeclInstanceMethod',
    'CountDeclInstanceVariable', 'CountDeclMethod', 'CountDeclMethodAll', 'CountDeclMethodDefault',
    'CountDeclMethodPrivate', 'CountDeclMethodProtected', 'CountDeclMethodPublic', 'CountLineBlank',
    'CountLineCode', 'CountLineCodeDecl', 'CountLineCodeExe', 'CountLineComment', 'CountSemicolon',
    'CountStmt', 'CountStmtDecl', 'CountStmtExe', 'MaxCyclomatic', 'MaxCyclomaticModified',
    'MaxCyclomaticStrict', 'MaxEssential', 'MaxInheritanceTree', 'MaxNesting', 'PercentLackOfCohesion',
    'RatioCommentToCode', 'SumCyclomatic', 'SumCyclomaticModified', 'SumCyclomaticStrict', 'SumEssential',
    'WOC', 'WMCNAMM', 'LOCNAMM', 'NOMNAMM', 'NOAM', 'TCC', 'PK_CountDeclClassMethod', 'NumberOfTokens',
    'NumberOfIdentifies', 'NumberOfReturnAndPrintStatements', 'NumberOfConditionalJumpStatements',
    'NumberOfKeywords', 'NumberOfAssignments', 'NumberOfOperatorsWithoutAssignments', 'NumberOfUniqueIdentifiers',
    'NumberOfDots', 'NumberOfNewStatements', 'MinLineCode', 'CountLineCodeNAMM', 'LogStmtDecl', 'CSNOMNAMM',
    'NIM', 'RFC', 'CFNAMM', 'DAC', 'NMO', 'NOII', 'LogCyclomaticStrict', 'FANIN', 'FANOUT', 'ATFD',
    'NumberOfMethodCalls', 'SumCountPath', 'NumberOfClassConstructors', 'AvgLineCodeExe', 'AvgStmtDecl',
    'NumberOfDepends'
]

def analyze_java_file(java_path, csv_path, target_class):
    # Tüm 81 metrik için başlangıçta 0.0 (Güvenlik Sübabı Kategori 4)
    m = {metrik: 0.0 for metrik in EGITIM_METRIK_SIRASI}

    # =========================================================================
    # BÖLÜM 1: SOURCEMETER "CLASS.CSV" OKUMASI
    # =========================================================================
    try:
        df = pd.read_csv(csv_path)
        sm_row = df[df['Name'] == target_class]
        if sm_row.empty:
            print(f"🚨 Hata: {target_class} sınıfı CSV içinde bulunamadı!")
            sys.exit(1)
        sm_data = sm_row.iloc[0]
    except Exception as e:
        print(f"🚨 CSV Okuma Hatası: {e}")
        sys.exit(1)

    # 1.1 Doğrudan Alınanlar (Kategori 1)
    m['LOC'] = float(sm_data.get('LOC', 0))
    m['CountDeclMethod'] = float(sm_data.get('NM', 0)) # NM = Toplam Metot
    m['SumCyclomatic'] = float(sm_data.get('WMC', 0))
    m['SumCyclomaticStrict'] = m['SumCyclomatic']
    m['SumCyclomaticModified'] = m['SumCyclomatic']
    m['MaxInheritanceTree'] = float(sm_data.get('DIT', 0))
    m['PercentLackOfCohesion'] = float(sm_data.get('LCOM5', 0))
    m['RFC'] = float(sm_data.get('RFC', 0))
    m['FANIN'] = float(sm_data.get('CBOI', 0))
    m['FANOUT'] = float(sm_data.get('CBO', 0))
    m['CountStmt'] = float(sm_data.get('NOS', 0))
    m['CountStmtDecl'] = float(sm_data.get('LDC', 0))
    m['CountLineComment'] = float(sm_data.get('CLOC', 0))
    m['CountDeclInstanceVariable'] = float(sm_data.get('NLA', 0))
    m['NOPA'] = float(sm_data.get('NPA', 0))
    m['CountClassCoupled'] = float(sm_data.get('CBO', 0))
    m['CountClassDerived'] = float(sm_data.get('NOC', 0))
    m['WOC'] = float(sm_data.get('WOC', 0))
    m['NOII'] = float(sm_data.get('NOI', 0))
    m['NMO'] = float(sm_data.get('NOP', 0))
    m['CountLineCode'] = float(sm_data.get('LLOC', 0))

    # 1.2 Matematiksel Çıkarımlar (Kategori 3 - NAMM vb.)
    nm = m['CountDeclMethod'] if m['CountDeclMethod'] > 0 else 1
    m['AvgCyclomatic'] = m['SumCyclomatic'] / nm
    m['AvgCyclomaticModified'] = m['AvgCyclomatic']
    m['AvgCyclomaticStrict'] = m['AvgCyclomatic']
    m['AvgLine'] = m['LOC'] / nm
    m['AvgLineCode'] = m['CountLineCode'] / nm
    m['CountStmtExe'] = max(0, m['CountStmt'] - m['CountStmtDecl'])
    m['AvgStmtDecl'] = m['CountStmtDecl'] / nm

    # Getter ve Setter hesabı
    nlg = float(sm_data.get('NLG', 0))
    nls = float(sm_data.get('NLS', 0))
    m['NOAM'] = nlg + nls
    m['NOMNAMM'] = max(0, m['CountDeclMethod'] - m['NOAM'])
    m['WMCNAMM'] = m['SumCyclomatic'] * (m['NOMNAMM'] / nm)
    m['LOCNAMM'] = m['LOC'] * (m['NOMNAMM'] / nm)
    m['CSNOMNAMM'] = m['FANOUT']

    m['NIM'] = max(0, m['CountDeclMethod'] - float(sm_data.get('NLM', 0)))
    m['CountDeclMethodAll'] = m['CountDeclMethod'] + m['NIM']
    m['CountDeclMethodPrivate'] = float(sm_data.get('NLPM', 0))
    m['CountDeclMethodPublic'] = float(sm_data.get('NPM', 0))

    m['LogCyclomaticStrict'] = math.log(m['SumCyclomaticStrict'] + 1)
    m['LogStmtDecl'] = math.log(m['CountStmtDecl'] + 1)

    # =========================================================================
    # BÖLÜM 2: JAVALANG İLE AST VE METİN ANALİZİ (Kategori 2)
    # =========================================================================
    with open(java_path, 'r', encoding='utf-8') as f:
        kod_metni = f.read()

    # Basit Metin (Regex) Sayımları
    m['CountSemicolon'] = float(kod_metni.count(';'))
    m['NumberOfDots'] = float(kod_metni.count('.'))
    m['CountLineBlank'] = float(len([s for s in kod_metni.split('\n') if not s.strip()]))
    m['NumberOfDepends'] = float(kod_metni.count('import '))

    try:
        # Kodu ağaca (AST) ve tokenlere ayır
        tokens = list(javalang.tokenizer.tokenize(kod_metni))
        tree = javalang.parse.parse(kod_metni)

        # Token Bazlı Metrikler
        m['NumberOfTokens'] = float(len(tokens))
        identifiers = [t.value for t in tokens if isinstance(t, javalang.tokenizer.Identifier)]
        m['NumberOfIdentifies'] = float(len(identifiers))
        m['NumberOfUniqueIdentifiers'] = float(len(set(identifiers)))
        m['NumberOfKeywords'] = float(len([t for t in tokens if isinstance(t, javalang.tokenizer.Keyword)]))
        m['NumberOfAssignments'] = float(len([t for t in tokens if t.value == '=']))

        operators = [t for t in tokens if isinstance(t, javalang.tokenizer.Operator)]
        m['NumberOfOperatorsWithoutAssignments'] = float(len([t for t in operators if t.value not in ['=', '==']]))

        # AST Ağacı Üzerinde Gezinti
        return_print_count = 0
        cond_jump_count = 0
        method_calls = 0
        class_creators = 0
        constructors = 0
        dac_count = 0
        atfd_count = 0

        for path, node in tree:
            if isinstance(node, (javalang.tree.IfStatement, javalang.tree.WhileStatement,
                                 javalang.tree.ForStatement, javalang.tree.SwitchStatement)):
                cond_jump_count += 1

            elif isinstance(node, javalang.tree.ReturnStatement):
                return_print_count += 1

            elif isinstance(node, javalang.tree.MethodInvocation):
                method_calls += 1
                if node.member in ['print', 'println']:
                    return_print_count += 1
                if node.qualifier is not None:
                    atfd_count += 1 # Başka sınıfın metodunu çağırma (ATFD)

            elif isinstance(node, javalang.tree.ClassCreator):
                class_creators += 1

            elif isinstance(node, javalang.tree.ConstructorDeclaration):
                constructors += 1

            elif isinstance(node, javalang.tree.FieldDeclaration):
                tip_ismi = node.type.name
                primitive_types = ['int', 'double', 'float', 'boolean', 'char', 'byte', 'short', 'long', 'String']
                if tip_ismi not in primitive_types:
                    dac_count += 1 # Data Abstraction Coupling

        m['NumberOfReturnAndPrintStatements'] = float(return_print_count)
        m['NumberOfConditionalJumpStatements'] = float(cond_jump_count)
        m['NumberOfMethodCalls'] = float(method_calls)
        m['NumberOfNewStatements'] = float(class_creators)
        m['NumberOfClassConstructors'] = float(constructors)
        m['DAC'] = float(dac_count)
        m['ATFD'] = float(atfd_count)

    except Exception as e:
        print(f"⚠️ Uyarı: javalang ayrıştırma hatası ({e}). İlgili AST metrikleri 0.0 olarak kalacak.")

    # =========================================================================
    # BÖLÜM 3: PAKETLE VE API İÇİN JSON'A YAZ
    # =========================================================================
    nihai_vektor = [m[metrik] for metrik in EGITIM_METRIK_SIRASI]

    payload = {
        "filename": f"{target_class}.java",
        "code_content": kod_metni,
        "metrics": nihai_vektor
    }

    with open('payload.json', 'w', encoding='utf-8') as f:
        json.dump(payload, f)

    print(f"✅ Başarılı! 81 boyutlu vektör oluşturuldu. Boyut: {len(nihai_vektor)}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Code Smell AI Adapter")
    parser.add_argument("--java", required=True, help="Değişen Java dosyasının tam yolu")
    parser.add_argument("--csv", required=True, help="SourceMeter'ın ürettiği Class.csv dosyasının tam yolu")
    parser.add_argument("--class_name", required=True, help="Analiz edilecek sınıfın adı")
    args = parser.parse_args()

    analyze_java_file(args.java, args.csv, args.class_name)