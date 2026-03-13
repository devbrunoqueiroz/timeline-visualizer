#!/bin/bash
set -e

BASE="http://localhost:8080/api/v1"
CT="Content-Type: application/json"

echo "=== Login ==="
TOKEN=$(curl -s -X POST "$BASE/auth/login" \
  -H "$CT" \
  -d '{"email":"seed@chronicle.dev","password":"seed123456"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
H="Authorization: Bearer $TOKEN"
echo "OK (${#TOKEN} chars)"

id_of() { echo "$1" | sed 's/.*"id":"\([^"]*\)".*/\1/'; }

mkline() {
  id_of "$(curl -s -X POST "$BASE/timelines" -H "$CT" -H "$H" -d "$1")"
}

mkev() {
  local tl="$1" title="$2" pos="$3" label="$4" content="$5"
  local body
  printf -v body '{"title":"%s","temporalPosition":%s,"temporalLabel":"%s","calendarSystem":"CUSTOM","contentText":"%s","contentType":"TEXT"}' \
    "$title" "$pos" "$label" "$content"
  id_of "$(curl -s -X POST "$BASE/timelines/$tl/events" -H "$CT" -H "$H" -d "$body")"
}

mkconn() {
  local src="$1" tgt="$2" type="$3" desc="$4"
  curl -s -X POST "$BASE/connections" -H "$CT" -H "$H" \
    -d "{\"sourceEventId\":\"$src\",\"targetEventId\":\"$tgt\",\"connectionType\":\"$type\",\"description\":\"$desc\"}" \
    > /dev/null && echo "  conn $type: $src -> $tgt"
}

# ── Timelines ──────────────────────────────────────────────────────────────
echo "=== Timelines ==="
TL_F=$(mkline '{"name":"Frodo: A Jornada do Anel","description":"Do Shire ao Monte da Perdicao. A espinha dorsal da historia.","visibility":"PUBLIC"}')
echo "Frodo:   $TL_F"
TL_A=$(mkline '{"name":"Aragorn: O Rei que Retorna","description":"Do Ranger ao Rei de Gondor e Arnor.","visibility":"PUBLIC"}')
echo "Aragorn: $TL_A"
TL_G=$(mkline '{"name":"Gandalf: O Cinzento e o Branco","description":"Morte, renascimento e o poder da sabedoria.","visibility":"PUBLIC"}')
echo "Gandalf: $TL_G"
TL_N=$(mkline '{"name":"Os Nazgul: Sombras em Perseguicao","description":"A implacavel cacada ao Portador do Anel.","visibility":"PUBLIC"}')
echo "Nazgul:  $TL_N"

# ── Eventos Frodo ──────────────────────────────────────────────────────────
echo "=== Frodo ==="
F1=$(mkev "$TL_F" "Frodo herda o Um Anel" 1 "Ano 3001 T.T." "Bilbo passa o Anel a Frodo. Gandalf avisa: mantenha-o secreto e seguro.")
echo "F1: $F1"
F2=$(mkev "$TL_F" "Gandalf revela a verdade" 5 "17 anos depois" "Gandalf confirma: e o Um Anel de Sauron. Frodo deve partir imediatamente.")
echo "F2: $F2"
F3=$(mkev "$TL_F" "Fuga do Shire" 10 "Saida do Shire" "Frodo, Sam, Merry e Pippin partem na escuridao. Os Nazgul ja farejam o Anel.")
echo "F3: $F3"
F4=$(mkev "$TL_F" "Encontro com Aragorn em Bree" 20 "Bree" "No Poney Saltitante, Frodo encontra Passolargo que os guia a Valfenda.")
echo "F4: $F4"
F5=$(mkev "$TL_F" "Ferimento no Amon Sul" 25 "Amon Sul" "O Rei-Bruxo apunhala Frodo com a Lamina de Morgul. O veneno comeca a transformar Frodo em espectro.")
echo "F5: $F5"
F6=$(mkev "$TL_F" "Chegada a Valfenda e cura" 30 "Valfenda" "Elrond remove o fragmento e cura Frodo. O Conselho decide o destino do Anel.")
echo "F6: $F6"
F7=$(mkev "$TL_F" "A Sociedade parte de Valfenda" 35 "Valfenda" "Nove caminhantes partem em segredo rumo ao Monte da Perdicao.")
echo "F7: $F7"
F8=$(mkev "$TL_F" "Minas de Moria e queda de Gandalf" 45 "Moria" "Gandalf cai na Ponte de Khazad-dum. A Sociedade foge devastada.")
echo "F8: $F8"
F9=$(mkev "$TL_F" "Dissolucao em Amon Hen" 60 "Amon Hen" "Boromir tenta tomar o Anel. Frodo foge sozinho. A Sociedade se dissolve.")
echo "F9: $F9"
F10=$(mkev "$TL_F" "Gollum aceito como guia" 75 "Margens do Anduin" "Frodo aceita Gollum como guia ate Mordor. Ve nele um reflexo do que pode se tornar.")
echo "F10: $F10"
F11=$(mkev "$TL_F" "Captura por Faramir em Henneth Annun" 90 "Henneth Annun" "Faramir captura Frodo e Sam mas, diferente de Boromir, escolhe deixa-los partir.")
echo "F11: $F11"
F12=$(mkev "$TL_F" "Gollum manipula Frodo contra Sam" 105 "Cirith Ungol" "Gollum divide Frodo e Sam. Frodo manda Sam embora — seu maior erro — e segue Gollum sozinho.")
echo "F12: $F12"
F13=$(mkev "$TL_F" "Shelob paralisa Frodo" 110 "Toca de Shelob" "A aranha Shelob paralisa Frodo. Gollum a havia guiado ate ele. Sam pensa que Frodo morreu.")
echo "F13: $F13"
F14=$(mkev "$TL_F" "Sam resgata Frodo" 120 "Torre de Cirith Ungol" "Sam descobre que Frodo esta vivo e invade a torre dos Orcs sozinho para resgate-lo.")
echo "F14: $F14"
F15=$(mkev "$TL_F" "Planuras de Gorgoroth" 130 "Mordor" "Frodo e Sam avancam por Mordor disfarados de orcs, exaustos e quase sem agua.")
echo "F15: $F15"
F16=$(mkev "$TL_F" "A Crise no Monte da Perdicao" 140 "Monte da Perdicao" "Frodo reivindica o Anel. Gollum morde fora o dedo e cai nas chamas com o Anel.")
echo "F16: $F16"
F17=$(mkev "$TL_F" "O Um Anel e destruido" 145 "Monte da Perdicao" "O Um Anel e destruido. Sauron cai. As aguilas resgatam Frodo e Sam.")
echo "F17: $F17"

# ── Eventos Aragorn ────────────────────────────────────────────────────────
echo "=== Aragorn ==="
A1=$(mkev "$TL_A" "Guia os hobbits de Bree a Valfenda" 20 "Bree" "Aragorn revela ser Passolargo e guia os hobbits carregando o peso do herdeiro oculto.")
echo "A1: $A1"
A2=$(mkev "$TL_A" "Defende no Amon Sul" 25 "Amon Sul" "Aragorn luta com tochas para afastar os Nazgul enquanto Frodo e apunhalado.")
echo "A2: $A2"
A3=$(mkev "$TL_A" "Conselho de Elrond" 32 "Valfenda" "Aragorn revela a Anduril remontada e aceita sua responsabilidade de conduzir a Sociedade.")
echo "A3: $A3"
A4=$(mkev "$TL_A" "Lidera apos Moria" 48 "Lothlorien" "Com Gandalf morto, Aragorn assume a lideranca da Sociedade. Galadriel o chama de Dunadan.")
echo "A4: $A4"
A5=$(mkev "$TL_A" "A escolha em Amon Hen" 60 "Amon Hen" "Aragorn escolhe salvar Merry e Pippin em vez de seguir Frodo. Entende que Frodo deve ir sozinho.")
echo "A5: $A5"
A6=$(mkev "$TL_A" "Batalha do Abismo de Helm" 85 "Abismo de Helm" "Aragorn lidera a defesa contra 10.000 Uruk-hai. Gandalf chega na aurora com os Rohirrim.")
echo "A6: $A6"
A7=$(mkev "$TL_A" "Os Caminhos dos Mortos" 115 "Caminhos dos Mortos" "Aragorn convoca o exercito dos mortos perjuros. Eles lhe devem obediencia desde a era de Isildur.")
echo "A7: $A7"
A8=$(mkev "$TL_A" "Batalha dos Campos de Pelennor" 125 "Pelennor" "Aragorn chega com os mortos-vivos e vira a batalha. Derrota as forcas de Sauron.")
echo "A8: $A8"
A9=$(mkev "$TL_A" "Ataque ao Portao Negro" 135 "Portao Negro" "Aragorn lidera o exercito ao Portao Negro como distracao para dar tempo a Frodo.")
echo "A9: $A9"
A10=$(mkev "$TL_A" "Coroacao de Aragorn" 150 "Minas Tirith" "Aragorn e coroado Rei Elessar. Casa-se com Arwen. A Quarta Era comeca.")
echo "A10: $A10"

# ── Eventos Gandalf ────────────────────────────────────────────────────────
echo "=== Gandalf ==="
G1=$(mkev "$TL_G" "Confirmacao do Anel e partida de Frodo" 5 "Shire" "Gandalf pesquisou por 17 anos nos arquivos de Minas Tirith. Confirmado: e o Um Anel. Ordena Frodo a partir.")
echo "G1: $G1"
G2=$(mkev "$TL_G" "Preso por Saruman em Orthanc" 8 "Orthanc" "Gandalf vai pedir ajuda mas Saruman ja traiu. E preso no topo da torre de Orthanc.")
echo "G2: $G2"
G3=$(mkev "$TL_G" "Resgatado por Gwaihir" 12 "Orthanc" "Gwaihir resgata Gandalf. Ele chega a Rohan e obtém Shadowfax, o mais rapido dos cavalos.")
echo "G3: $G3"
G4=$(mkev "$TL_G" "Enfrenta o Balrog em Khazad-dum" 45 "Ponte de Khazad-dum" "Gandalf enfrenta o Balrog de Morgoth: nao passaras! Ambos caem no abismo.")
echo "G4: $G4"
G5=$(mkev "$TL_G" "Combate e morte nas profundezas" 50 "Abismos de Moria" "Gandalf combate o Balrog por dez dias pelas profundezas. Mata a criatura mas morre tambem. Enviado de volta.")
echo "G5: $G5"
G6=$(mkev "$TL_G" "Retorna como Gandalf o Branco" 55 "Fangorn" "Gandalf retorna como Gandalf o Branco, mais poderoso. Assume o papel que Saruman abandonou.")
echo "G6: $G6"
G7=$(mkev "$TL_G" "Quebra o poder de Saruman" 70 "Orthanc" "Gandalf confronta Saruman em Orthanc e quebra o cajado do mago traidor.")
echo "G7: $G7"
G8=$(mkev "$TL_G" "Defesa de Minas Tirith" 125 "Minas Tirith" "Gandalf lidera a defesa de Minas Tirith. Enfrenta o Rei-Bruxo no portao ate a chegada de Theoden.")
echo "G8: $G8"

# ── Eventos Nazgul ─────────────────────────────────────────────────────────
echo "=== Nazgul ==="
N1=$(mkev "$TL_N" "Nazgul chegam ao Shire" 10 "Shire" "Os Nove Cavaleiros Negros chegam ao Shire farejando o Anel. Causam terror nos aldeoes.")
echo "N1: $N1"
N2=$(mkev "$TL_N" "Emboscada no Amon Sul" 25 "Amon Sul" "Cinco Nazgul atacam Frodo no alto do Amon Sul. O Rei-Bruxo apunhala Frodo com a Lamina de Morgul.")
echo "N2: $N2"
N3=$(mkev "$TL_N" "Perseguicao ate o Rio Bruinen" 28 "Rio Bruinen" "Os Nazgul perseguem Frodo. Elrond convoca uma enchente e os cavalos espectrais sao destruidos.")
echo "N3: $N3"
N4=$(mkev "$TL_N" "Recebem as Fell Beasts" 80 "Mordor" "Sauron concede Fell Beasts aos Nazgul. Agora dominam os ceus sobre Gondor.")
echo "N4: $N4"
N5=$(mkev "$TL_N" "Rei-Bruxo mata Theoden em Pelennor" 125 "Pelennor" "O Rei-Bruxo derruba Theoden. Eowyn e Merry o destroem: nenhum homem o matara.")
echo "N5: $N5"
N6=$(mkev "$TL_N" "Dissolucao com a destruicao do Anel" 145 "Mordor" "Com a destruicao do Um Anel, os Nazgul perdem sua essencia e se dissolvem no ar.")
echo "N6: $N6"

# ── Conexoes ───────────────────────────────────────────────────────────────
echo ""
echo "=== Conexoes ==="

# Frodo linha causal interna
mkconn "$F1" "$F2" "CAUSAL" "Herdar o Anel forcou Gandalf a revelar a verdade anos depois"
mkconn "$F2" "$F3" "CAUSAL" "A revelacao da verdade causou a fuga imediata do Shire"
mkconn "$F3" "$F4" "TEMPORAL" "A fuga levou os hobbits a Bree onde encontraram Aragorn"
mkconn "$F6" "$F7" "PREREQUISITE" "A cura em Valfenda era prerequisito para a Sociedade partir"
mkconn "$F8" "$F9" "ESCALATION" "A perda de Gandalf escalou para a dissolucao da Sociedade"
mkconn "$F9" "$F10" "CAUSAL" "A dissolucao forcou Frodo a aceitar Gollum como unico guia"
mkconn "$F12" "$F13" "CAUSAL" "Mandar Sam embora causou diretamente a emboscada de Shelob"
mkconn "$F13" "$F14" "RESOLUTION" "O resgate de Sam resolveu a captura por Shelob"
mkconn "$F16" "$F17" "CAUSAL" "A crise no Monte da Perdicao foi a causa direta da destruicao do Anel"

# Cruzamentos Frodo x Aragorn
mkconn "$F4" "$A1" "PARALLEL" "Frodo e Aragorn se encontram em Bree: destinos entrelacados"
mkconn "$F5" "$A2" "PARALLEL" "Ambos vivem o ataque do Amon Sul simultaneamente"
mkconn "$F6" "$A3" "PARALLEL" "Frodo e curado enquanto Aragorn se compromete no Conselho"
mkconn "$F8" "$A4" "PARALLEL" "A perda de Gandalf transforma tanto Frodo quanto Aragorn"
mkconn "$F9" "$A5" "PARALLEL" "A dissolucao afeta ambos: Frodo foge, Aragorn faz sua escolha"
mkconn "$A9" "$F17" "PREREQUISITE" "O ataque ao Portao Negro era prerequisito para Frodo destruir o Anel"

# Cruzamentos Frodo x Gandalf
mkconn "$F2" "$G1" "PARALLEL" "Gandalf revela a verdade a Frodo: o mesmo evento, perspectivas diferentes"
mkconn "$F8" "$G4" "PARALLEL" "A queda de Gandalf em Moria e o trauma de Frodo: o mesmo momento"
mkconn "$G6" "$F8" "FORESHADOW" "O renascimento de Gandalf foi presagiado por sua queda heroica"
mkconn "$G3" "$F7" "PREREQUISITE" "Gandalf precisava estar livre para liderar a Sociedade"

# Cruzamentos Frodo x Nazgul
mkconn "$F3" "$N1" "PARALLEL" "Frodo foge do Shire enquanto os Nazgul chegam la: forcas opostas no mesmo espaco"
mkconn "$N2" "$F5" "CAUSAL" "A emboscada dos Nazgul no Amon Sul causou o ferimento de Frodo"
mkconn "$N3" "$F6" "PREREQUISITE" "A destruicao dos cavalos Nazgul foi prerequisito para Frodo chegar a Valfenda"
mkconn "$F17" "$N6" "CAUSAL" "A destruicao do Anel foi a causa direta da dissolucao dos Nazgul"

# Aragorn x Gandalf
mkconn "$G6" "$A4" "FORESHADOW" "O retorno de Gandalf o Branco presagiou o retorno do Rei"
mkconn "$G4" "$A4" "CAUSAL" "A queda de Gandalf causou Aragorn a assumir a lideranca"
mkconn "$G7" "$A6" "PREREQUISITE" "Quebrar Saruman foi prerequisito para a vitoria no Abismo de Helm"
mkconn "$G8" "$A8" "PARALLEL" "Gandalf defende Minas Tirith enquanto Aragorn avanca pelos Caminhos dos Mortos"

# Aragorn x Nazgul
mkconn "$A2" "$N2" "CONTRAST" "Aragorn defende com fogo enquanto os Nazgul atacam com sombra: contraste"
mkconn "$A8" "$N5" "PARALLEL" "A batalha de Pelennor: o climax de ambas as historias ao mesmo tempo"
mkconn "$N5" "$A8" "ESCALATION" "A morte de Theoden escalou para a vitoria final de Aragorn em Pelennor"

# Gandalf x Nazgul
mkconn "$G2" "$N1" "CONTRAST" "Gandalf preso enquanto os Nazgul agem livremente: a sombra avanca"
mkconn "$G8" "$N5" "CONTRAST" "Gandalf enfrenta o Rei-Bruxo no portao; Eowyn o mata nos campos: mesma ameaca, caminhos opostos"

# Arcos foreshadow de longo alcance
mkconn "$F1" "$F16" "FORESHADOW" "Herdar o Anel presagiou desde o inicio a crise final no Monte da Perdicao"
mkconn "$G2" "$G6" "FORESHADOW" "O aprisionamento de Gandalf presagiou sua morte e renascimento"
mkconn "$A3" "$A10" "FORESHADOW" "Revelar a Anduril no Conselho presagiou a coroacao de Aragorn"
mkconn "$N1" "$N6" "FORESHADOW" "A chegada dos Nazgul ao Shire presagiou sua dissolucao final"
mkconn "$F10" "$F16" "FORESHADOW" "Aceitar Gollum como guia presagiou o papel crucial de Gollum no Monte da Perdicao"

echo ""
echo "=== SEED COMPLETO ==="
echo "Timelines: Frodo=$TL_F | Aragorn=$TL_A | Gandalf=$TL_G | Nazgul=$TL_N"
echo "Acesse: http://localhost:4200"
