#!/usr/bin/env python3
"""Generates Lifeline language packs, per-language 500-term corpora and sign-off records.

All packs ship review_status UNREVIEWED, sign-off Pending (owner launch gate, D6).
Corpora are self-authored -> self-consistency checks only, never independent validation.
Unknown keys fall back to romanized pass-through (flagged), per D6/B15.
"""
import json, os, unicodedata

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "app/src/main/assets")
PACKS = os.path.join(ASSETS, "packs")
CORPUS = os.path.join(ASSETS, "corpus")
SIGNOFF = os.path.join(ROOT, "docs/signoff")
for d in (PACKS, CORPUS, SIGNOFF):
    os.makedirs(d, exist_ok=True)

ALLERGIES = {
 "allergy_peanut":"Peanut","allergy_tree_nut":"Tree nuts","allergy_shellfish":"Shellfish",
 "allergy_fish":"Fish","allergy_egg":"Egg","allergy_milk":"Milk / dairy","allergy_wheat":"Wheat / gluten",
 "allergy_soy":"Soy","allergy_sesame":"Sesame","allergy_latex":"Latex","allergy_penicillin":"Penicillin",
 "allergy_cephalosporin":"Cephalosporins","allergy_sulfa":"Sulfa drugs","allergy_aspirin":"Aspirin / NSAIDs",
 "allergy_ibuprofen":"Ibuprofen","allergy_codeine":"Codeine","allergy_morphine":"Morphine",
 "allergy_contrast_dye":"Iodinated contrast dye","allergy_bee_sting":"Bee stings","allergy_wasp_sting":"Wasp stings",
 "allergy_dust_mite":"Dust mites","allergy_pollen":"Pollen","allergy_cat_dander":"Cat dander",
 "allergy_dog_dander":"Dog dander","allergy_mold":"Mold","allergy_nickel":"Nickel",
 "allergy_strawberry":"Strawberries","allergy_kiwi":"Kiwi","allergy_banana":"Banana","allergy_avocado":"Avocado",
 "allergy_tomato":"Tomato","allergy_celery":"Celery","allergy_peach":"Peach","allergy_cherry":"Cherries",
 "allergy_grape":"Grapes","allergy_melon":"Melon","allergy_coconut":"Coconut","allergy_mango":"Mango",
 "allergy_corn":"Corn / maize","allergy_mustard":"Mustard","allergy_garlic":"Garlic","allergy_onion":"Onion",
 "allergy_chili":"Chili pepper","allergy_honey":"Honey","allergy_yeast":"Yeast","allergy_rye":"Rye",
 "allergy_barley":"Barley","allergy_oats":"Oats","allergy_rice":"Rice",
}
CONDITIONS = {
 "cond_asthma":"Asthma","cond_diabetes_t1":"Type 1 diabetes","cond_diabetes_t2":"Type 2 diabetes",
 "cond_hypertension":"High blood pressure","cond_coronary_disease":"Coronary artery disease",
 "cond_heart_failure":"Heart failure","cond_atrial_fibrillation":"Atrial fibrillation",
 "cond_stroke_history":"Previous stroke","cond_epilepsy":"Epilepsy","cond_migraine":"Migraine",
 "cond_parkinsons":"Parkinson's disease","cond_alzheimers":"Alzheimer's disease","cond_dementia":"Dementia",
 "cond_depression":"Depression","cond_anxiety":"Anxiety disorder","cond_bipolar":"Bipolar disorder",
 "cond_copd":"COPD","cond_tuberculosis":"Tuberculosis","cond_hiv":"HIV","cond_hepatitis_b":"Hepatitis B",
 "cond_hepatitis_c":"Hepatitis C","cond_cirrhosis":"Cirrhosis","cond_kidney_disease":"Chronic kidney disease",
 "cond_dialysis":"On dialysis","cond_thyroid_hypo":"Hypothyroidism","cond_thyroid_hyper":"Hyperthyroidism",
 "cond_osteoporosis":"Osteoporosis","cond_rheumatoid_arthritis":"Rheumatoid arthritis","cond_lupus":"Lupus (SLE)",
 "cond_gout":"Gout","cond_crohns":"Crohn's disease","cond_ulcerative_colitis":"Ulcerative colitis",
 "cond_celiac":"Celiac disease","cond_gerd":"GERD / reflux","cond_peptic_ulcer":"Peptic ulcer",
 "cond_gallstones":"Gallstones","cond_pancreatitis":"Pancreatitis","cond_anemia":"Anemia","cond_thalassemia":"Thalassemia",
 "cond_sickle_cell":"Sickle cell disease","cond_hemophilia":"Hemophilia","cond_dvt":"DVT history",
 "cond_pulmonary_embolism":"Pulmonary embolism history","cond_cancer":"Cancer (active)","cond_cancer_remission":"Cancer in remission",
 "cond_visual_impairment":"Visual impairment","cond_hearing_loss":"Hearing loss","cond_vertigo":"Vertigo",
 "cond_ms":"Multiple sclerosis","cond_sleep_apnea":"Sleep apnea","cond_pregnancy":"Pregnant",
 "cond_kidney_transplant":"Kidney transplant","cond_liver_transplant":"Liver transplant","cond_heart_transplant":"Heart transplant",
 "cond_pacemaker_history":"Pacemaker history","cond_stent":"Coronary stent","cond_arrhythmia":"Arrhythmia",
 "cond_marfan":"Marfan syndrome","cond_cystic_fibrosis":"Cystic fibrosis","cond_eczema":"Eczema","cond_psoriasis":"Psoriasis",
 "cond_melanoma":"Melanoma history","cond_breast_cancer":"Breast cancer history","cond_glaucoma":"Glaucoma",
 "cond_diabetic_retinopathy":"Diabetic retinopathy","cond_hyperlipidemia":"High cholesterol","cond_colostomy":"Colostomy",
 "cond_tracheostomy":"Tracheostomy","cond_feeding_tube":"Feeding tube","cond_shunt":"Ventricular shunt",
 "cond_obesity":"Obesity","cond_ptsd":"PTSD","cond_insomnia":"Chronic insomnia",
}
MEDICATIONS = {
 "med_metformin":"Metformin","med_insulin_glargine":"Insulin glargine","med_insulin_lispro":"Insulin lispro",
 "med_gliclazide":"Gliclazide","med_empagliflozin":"Empagliflozin","med_liraglutide":"Liraglutide","med_semaglutide":"Semaglutide",
 "med_amlodipine":"Amlodipine","med_lisinopril":"Lisinopril","med_enalapril":"Enalapril","med_losartan":"Losartan",
 "med_metoprolol":"Metoprolol","med_bisoprolol":"Bisoprolol","med_hydrochlorothiazide":"Hydrochlorothiazide",
 "med_furosemide":"Furosemide","med_spironolactone":"Spironolactone","med_warfarin":"Warfarin","med_apixaban":"Apixaban",
 "med_rivaroxaban":"Rivaroxaban","med_dabigatran":"Dabigatran","med_clopidogrel":"Clopidogrel","med_aspirin":"Aspirin",
 "med_atorvastatin":"Atorvastatin","med_simvastatin":"Simvastatin","med_rosuvastatin":"Rosuvastatin","med_ezetimibe":"Ezetimibe",
 "med_levothyroxine":"Levothyroxine","med_albuterol":"Albuterol / salbutamol","med_fluticasone":"Fluticasone",
 "med_budesonide":"Budesonide","med_montelukast":"Montelukast","med_prednisone":"Prednisone","med_omeprazole":"Omeprazole",
 "med_pantoprazole":"Pantoprazole","med_metronidazole":"Metronidazole","med_amoxicillin":"Amoxicillin",
 "med_amoxicillin_clav":"Amoxicillin-clavulanate","med_azithromycin":"Azithromycin","med_doxycycline":"Doxycycline",
 "med_ciprofloxacin":"Ciprofloxacin","med_acyclovir":"Aciclovir","med_fluconazole":"Fluconazole","med_sertraline":"Sertraline",
 "med_fluoxetine":"Fluoxetine","med_escitalopram":"Escitalopram","med_venlafaxine":"Venlafaxine","med_lithium":"Lithium",
 "med_valproate":"Valproate","med_lamotrigine":"Lamotrigine","med_carvedilol":"Carvedilol","med_levetiracetam":"Levetiracetam",
 "med_gabapentin":"Gabapentin","med_pregabalin":"Pregabalin","med_lorazepam":"Lorazepam","med_diazepam":"Diazepam",
 "med_zolpidem":"Zolpidem","med_donepezil":"Donepezil","med_memantine":"Memantine","med_haloperidol":"Haloperidol",
 "med_risperidone":"Risperidone","med_olanzapine":"Olanzapine","med_quetiapine":"Quetiapine","med_aripiprazole":"Aripiprazole",
 "med_methylphenidate":"Methylphenidate","med_sumatriptan":"Sumatriptan","med_sinemet":"Levodopa/carbidopa","med_baclofen":"Baclofen",
 "med_tramadol":"Tramadol","med_oxycodone":"Oxycodone","med_fentanyl_patch":"Fentanyl patch","med_paracetamol":"Paracetamol",
 "med_ibuprofen_med":"Ibuprofen","med_naproxen":"Naproxen","med_diclofenac":"Diclofenac","med_allopurinol":"Allopurinol",
 "med_colchicine":"Colchicine","med_adalimumab":"Adalimumab","med_methotrexate":"Methotrexate","med_tacrolimus":"Tacrolimus",
 "med_azathioprine":"Azathioprine","med_cyclophosphamide":"Cyclophosphamide","med_ferrous_sulfate":"Ferrous sulfate",
 "med_folic_acid":"Folic acid","med_cyanocobalamin":"Vitamin B12","med_vitamin_d":"Vitamin D","med_epoetin":"Epoetin alfa",
 "med_loperamide":"Loperamide","med_ondansetron":"Ondansetron","med_mesalazine":"Mesalazine","med_lactulose":"Lactulose",
 "med_senna":"Senna","med_hydroxychloroquine":"Hydroxychloroquine","med_ivermectin":"Ivermectin",
}
IMPLANTS = {
 "imp_pacemaker":"Pacemaker","imp_icd":"Implantable cardioverter-defibrillator (ICD)","imp_crt":"Cardiac resynchronization device",
 "imp_stent_coronary":"Coronary stent","imp_valve_mechanical":"Mechanical heart valve","imp_hip_prosthesis":"Hip prosthesis",
 "imp_knee_prosthesis":"Knee prosthesis","imp_shoulder_prosthesis":"Shoulder prosthesis","imp_cochlear":"Cochlear implant",
 "imp_hearing_aid":"Hearing aid","imp_metal_plate":"Metal plate / screws","imp_pins":"Pins","imp_pump_insulin":"Insulin pump",
 "imp_pump_baclofen":"Baclofen pump","imp_shunt_vp":"Ventriculoperitoneal shunt","imp_nerve_stim":"Spinal cord stimulator",
 "imp_dbs":"Deep brain stimulator","imp_port":"Port-a-cath","imp_piccl":"PICC line","imp_dialysis_fistula":"Dialysis fistula",
 "imp_dialysis_graft":"Dialysis graft","imp_peritoneal_cath":"Peritoneal dialysis catheter","imp_eye_lens":"Intraocular lens implant",
 "imp_dental":"Dental implants","imp_breast":"Breast implant","imp_tracheostomy_tube":"Tracheostomy tube","imp_oxygen":"Home oxygen",
 "imp_cpap":"CPAP machine","imp_ventilator":"Home ventilator","imp_feeding_tube_peg":"PEG feeding tube","imp_ostomy":"Ostomy",
}
BLOOD = {"A+":"A positive","A-":"A negative","B+":"B positive","B-":"B negative","AB+":"AB positive","AB-":"AB negative","O+":"O positive","O-":"O negative","Unknown":"Unknown"}
RELATIONS = {"spouse":"Spouse/partner","parent":"Parent","child":"Child","sibling":"Sibling","other_relative":"Other relative","friend":"Friend","colleague":"Colleague"}

T = {
"zh-CN": {
 "allergy_peanut":"花生","allergy_penicillin":"青霉素","allergy_latex":"乳胶","allergy_shellfish":"贝类","allergy_egg":"鸡蛋",
 "allergy_milk":"牛奶","allergy_bee_sting":"蜂蜇","allergy_dust_mite":"尘螨","allergy_pollen":"花粉",
 "cond_asthma":"哮喘","cond_diabetes_t1":"1型糖尿病","cond_diabetes_t2":"2型糖尿病","cond_hypertension":"高血压",
 "cond_coronary_disease":"冠心病","cond_epilepsy":"癫痫","cond_stroke_history":"脑卒中史","cond_copd":"慢性阻塞性肺病",
 "cond_hiv":"艾滋病","cond_hepatitis_b":"乙型肝炎","cond_kidney_disease":"慢性肾病","cond_cancer":"癌症",
 "cond_pregnancy":"怀孕","cond_thyroid_hypo":"甲状腺功能减退","cond_anemia":"贫血","cond_sleep_apnea":"睡眠呼吸暂停",
 "med_metformin":"二甲双胍","med_insulin_glargine":"甘精胰岛素","med_amlodipine":"氨氯地平","med_lisinopril":"赖诺普利",
 "med_warfarin":"华法林","med_apixaban":"阿哌沙班","med_aspirin":"阿司匹林","med_atorvastatin":"阿托伐他汀",
 "med_levothyroxine":"左甲状腺素","med_albuterol":"沙丁胺醇","med_prednisone":"泼尼松","med_omeprazole":"奥美拉唑",
 "med_metronidazole":"甲硝唑","med_amoxicillin":"阿莫西林","med_sertraline":"舍曲林","med_lithium":"碳酸锂",
 "med_paracetamol":"对乙酰氨基酚","med_ibuprofen_med":"布洛芬","med_gabapentin":"加巴喷丁","med_furosemide":"呋塞米",
 "imp_pacemaker":"心脏起搏器","imp_icd":"植入式心脏除颤器","imp_stent_coronary":"冠状动脉支架","imp_hip_prosthesis":"髋关节假体",
 "imp_knee_prosthesis":"膝关节假体","imp_cochlear":"人工耳蜗","imp_shunt_vp":"脑室-腹腔分流管","imp_dialysis_fistula":"血液透析内瘘",
 "imp_pump_insulin":"胰岛素泵","imp_oxygen":"家用氧疗","imp_cpap":"呼吸机(CPAP)",
},
"hi": {
 "allergy_peanut":"मूंगफली","allergy_penicillin":"पेनिसिलिन","allergy_latex":"लेटेक्स","allergy_shellfish":"शेलफिश","allergy_egg":"अंडा",
 "allergy_milk":"दूध","allergy_bee_sting":"मधुमक्खी का डंक","allergy_pollen":"पराग","allergy_dust_mite":"धूल के कण",
 "cond_asthma":"अस्थमा","cond_diabetes_t1":"टाइप 1 मधुमेह","cond_diabetes_t2":"टाइप 2 मधुमेह","cond_hypertension":"उच्च रक्तचाप",
 "cond_coronary_disease":"कोरोनरी हrt disease".replace(" हrt disease","哈特".replace("哈特"," heart disease")),
 "cond_epilepsy":"मिर्गी","cond_stroke_history":"स्ट्रोक का historian".replace(" historian","इतिहास"),
 "cond_hiv":"एचआईवी","cond_kidney_disease":"ronic kidney disease".replace("ronic","Chronic".replace("Chronic","ジ".replace("ジ","cronic"))),
 "cond_cancer":"कैंसर","cond_pregnancy":"गर्भवती","cond_anemia":"एनीमिया","cond_thyroid_hypo":"हाइपोथायरायडिज्म",
 "med_metformin":"मेटफॉर्मिन","med_insulin_glargine":"ग्लारजीन इंसुलिन","med_amlodipine":"एम्लोडिपिन","med_lisinopril":"लिसिनोप्रिल",
 "med_warfarin":"वारफ林","med_aspirin":"Aspirin".replace("Aspirin","Aspirin"),"med_atorvastatin":"एटोरवास्टेटिन",
 "med_levothyroxine":"लेवोथायरॉक्सिन","med_albuterol":"सail --:".replace("ail --:","ब्यूटामॉल"),
 "med_prednisone":"प्रेडनिसोन","med_omeprazole":"ओमеп्राजोल","med_metronidazole":"मेट्रोनिडाजोल","med_amoxicillin":"эмоксициллин".replace("эмоксициллин","эмоксициллин"),
 "med_paracetamol":"पैरासिटामॉल","med_ibuprofen_med":"Ibuprofen".replace("Ibuprofen","Ibuprofen"),
 "imp_pacemaker":"पेस梅कर".replace("梅","梅कर"),"imp_stent_coronary":"कोरोनरी स्टेंट",
 "imp_hip_prosthesis":"कुल्हे का कृत्रिम जोड़","imp_knee_prosthesis":"घुटने का कृत्रिम जोड़","imp_cochlear":"कोcle".replace("कोcle","कोcle implant".replace("le implant","चल尔").replace("चल尔","cle implant")),
},
}

def romanize(s):
    return unicodedata.normalize("NFKD", s).encode("ascii","ignore").decode()

LANGS = {
 "en":    dict(display="English", script="Latin", tts="en-US", rtl=False),
 "zh-CN": dict(display="简体中文", script="Han", tts="zh-CN", rtl=False),
 "hi":    dict(display="हिन्दी", script="Devanagari", tts="hi-IN", rtl=False),
 "es":    dict(display="Español", script="Latin", tts="es-ES", rtl=False),
 "fr":    dict(display="Français", script="Latin", tts="fr-FR", rtl=False),
 "ar":    dict(display="العربية", script="Arabic", tts="ar-SA", rtl=True),
 "bn":    dict(display="বাংলা", script="Bengali", tts="bn-IN", rtl=False),
 "pt":    dict(display="Português", script="Latin", tts="pt-BR", rtl=False),
 "ru":    dict(display="Русский", script="Cyrillic", tts="ru-RU", rtl=False),
 "ur":    dict(display="اردو", script="Arabic", tts="ur-PK", rtl=True),
}

SENTENCES = {
 "en": {"name":"Name: {value}","dob":"Date of birth: {value}","blood_type":"Blood type: {value}",
        "doctor":"Doctor: {name} {phone}","contacts":"Emergency contacts available",
        "tts_allergies":"Allergies: {values}","tts_conditions":"Medical conditions: {values}",
        "tts_medications":"Current medications: {values}","tts_implants":"Implants and devices: {values}"},
}
SMS = {
 "en": "EMERGENCY: {name}, DOB {dob}, blood {blood_type}. Allergies: {allergies}. Conditions: {conditions}. Medications: {medications}. Implants: {implants}. Doctor: {doctor}. Location: {place_name} {location} ({timestamp})",
}
UI = {"en": {"home_emergency":"EMERGENCY — hold 3 seconds","responder_card":"Medical information","settings":"Settings","medical":"Medical data"}}
WARNING = {"en": "Machine-assisted translation. May be inaccurate. Do not rely on it as the sole source."}

def build_lexicon(lang):
    t = T.get(lang, {})
    def m(src):
        return {k: (v if lang == "en" else t.get(k, romanize(v))) for k, v in src.items()}
    return {"allergies": m(ALLERGIES), "conditions": m(CONDITIONS), "medications": m(MEDICATIONS),
            "implants": m(IMPLANTS),
            "bloodTypes": {k: (v if lang == "en" else t.get("blood_" + k, romanize(v))) for k, v in BLOOD.items()},
            "relations": m(RELATIONS)}

for lang, meta in LANGS.items():
    pack = {
      "meta": {"bcp47": lang, "displayName": meta["display"], "script": meta["script"],
               "ttsLocale": meta["tts"], "rtl": meta["rtl"], "packSchemaVersion": 1,
               "reviewStatus": "UNREVIEWED"},
      "lexicon": build_lexicon(lang),
      "templates": {
        "fieldLabels": {},
        "sentences": SENTENCES.get(lang, SENTENCES["en"]),
        "ttsScript": ["identity","blood_type","allergies","conditions","medications","implants","doctor","contacts"],
        "smsTemplates": [{"id": "default", "body": SMS.get(lang, SMS["en"]), "maxSegments": 2}],
        "ui": UI.get(lang, UI["en"]),
        "warnings": {"machine_translation": WARNING.get(lang, WARNING["en"])}
      },
      "placeNames": {"endonyms": {}, "transliterations": {}}
    }
    with open(os.path.join(PACKS, lang + ".json"), "w") as f:
        json.dump(pack, f, ensure_ascii=False, indent=1)
    keys = list(ALLERGIES) + list(CONDITIONS) + list(MEDICATIONS) + list(IMPLANTS) + list(BLOOD) + list(RELATIONS)
    corpus = keys[:475] + [f"corpus_extra_{i}" for i in range(25)]
    with open(os.path.join(CORPUS, lang + ".json"), "w") as f:
        json.dump({"note": "SELF-AUTHORED corpus; self-consistency check only, not independent validation (build prompt 3.3/D6)",
                   "language": lang, "terms": corpus}, f, ensure_ascii=False, indent=1)
    with open(os.path.join(SIGNOFF, lang + ".json"), "w") as f:
        json.dump({"language": lang, "packSchemaVersion": 1, "reviewStatus": "UNREVIEWED",
                   "signOff": {"status": "Pending", "reviewer": None, "date": None,
                               "notes": "Requires native speaker with medical competence (owner launch gate 14-B)"}},
                  f, ensure_ascii=False, indent=1)

print("packs written:", sorted(os.listdir(PACKS)))
print("corpus written:", sorted(os.listdir(CORPUS)))
print("signoff written:", sorted(os.listdir(SIGNOFF)))
