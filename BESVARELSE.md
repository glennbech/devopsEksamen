# BESVARELSE KANDIDAT 2012 DEVOPS EKSAMEN
## For at alt skal fungere med sensor  

### Git
Sensoren må lage en kopi (fork) av følgende repository: https://github.com/freebattie/devopsEksamen

### AWS
Sensoren må logge inn på AWS-kontoen, navigere til IAM, deretter til "users", gå inn på sin bruker og velge "Create access key". Deretter velger man CLI, gir nøkkelen et navn og kopierer verdiene for Access key og Secret access key. Se vedlagt bilde av en gammel og deaktivert nøkkel
![aws_key_val.png](..%2F..%2F..%2FUsers%2Fbjart%2FDesktop%2Faws_key_val.png)
isse verdiene skal legges inn i GitHub Action secrets, som vil bli forklart nærmere nedenfor. Pass på å ikke lukke denne siden før du har lagt inn verdiene i GitHug og i cmd i oppgave1 B

### Github
Sensoren må opprette to secrets ved å gå inn på sitt repository og velge: Settings > "Secrets and variables" > Actions.  
Her skal sensor lage til to secrets:  
AWS_ACCESS_KEY_ID  
AWS_SECRET_ACCESS_KEY  
MAIL
Under "Name" for det første legger du inn "AWS_ACCESS_KEY_ID", og verdien "Access key" fra AWS som "Secret". Deretter trykker du på "Add secret". Deretter oppretter du en til med "AWS_SECRET_ACCESS_KEY" som "Name", og "Secret access key" fra AWS som "Secret". Se
![github_secret.png](..%2F..%2F..%2FUsers%2Fbjart%2FDesktop%2Fgithub_secret.png)
på mail så legger du inn mailen som du vill alarmen i oppgave 4 skal testes mot
### GitHub ACTION 

for sam_deploy_main.yml må følgendes endres:  
S3_IMAGE_BUCKET - Her legger du inn hvor SAM skal hente bilder fra
STACK_NAME - Denne må endres til unikt navn på SAM appen
S3_ARTIFACT - Denne trenges kun å endres om sensor vill burke en annen bucket for SAM sin configurasjon

for terraform_apprunner_deploy_aws.yml:


## OPPAGAVE 1 A

jeg har endret  API kallet fra /hello til /check. 
Det er også gjort andre tilpasninger i template-filen 
for å tydeligere gjenspeile funksjonaliteten.
Jeg har laget 2 work flows som heter sam_deploy_main.yml og sam_deploy_main.yml
for sam_deploy_main.yml må følgendes endres:  
S3_IMAGE_BUCKET - Her legger du inn hvor SAM skal hente bilder fra
STACK_NAME - Denne må endres til unikt navn på SAM appen
S3_ARTIFACT - Denne trenges kun å endres om sensor vill burke en annen bucket for SAM sin configurasjon
sam_deploy_main.yml trenges ingen ting å endres

## OPPAGAVE 1 B
Filen ligger i mappen Kjell/ppe_check siden jeg valgte å rename alt til å bedre stemme med funksjonalitet
du må manuelt skifte ut XXX, YYY og kjellsimagebucket med sensors hemmligeter  og bilde s3 bucket
```
docker build -t kjellpy .
docker run -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket kjellpy
```

## OPPGAVE 2 A
Dockerfilen ligger i root folder
du må manuelt skifte ut XXX, YYY og kjellsimagebucket med sensors hemmligeter  og bilde s3 bucket
``` 
docker build -t ppe .
docker run -p 8080:8080 -e AWS_ACCESS_KEY_ID=XXX -e AWS_SECRET_ACCESS_KEY=YYY -e BUCKET_NAME=kjellsimagebucket ppe```
```
## OPPGAVE 2 B
mitt ECR er student2012-private

## OPPGAVE 3 A
variabler så velger jeg å lage til for image, prefix og port
Port er satt til default 8080, med mulig het til å endre om det trengs
prefix er for service navne 
image er for docker image

## OPPGAVE 4
mapper i s3 bucket:
camera/pictures - her skal alle bilde ligge includert en copy av bilder som blir lagt inn i employee detter er kun for at simuleringen skal fungere
camera/empolyee - her skal det ligge bilder av alle som er ansatt i bedriften
camera/private - mappen som kunnden bruker for å laste opp bilder fra private ingangs camera
camera/entrence - mappen som kunden bruker for å laste opp bilder fra offentlig inngangs kamera 
camera/exit - mappen som kunden lsater opp bilder fra utgangs kamera  

### API
for CameraController:
Get /scan-private-entrance-automatic
Get /scan-private-entrance-manual
Get /scan-exit-manual
Get /scan-exit-automatic
Get /scan-public-entrance-manual
Get /scan-public-entrance-automatic
Get /get-list-of-persons-in-building

for RekognitionController
Get / scan-custom-ppe
### Funcsjonalitet CameraController
Selskapet har nå utvidet med to nye funksjoner. Den første funksjonen gjør det mulig for selskaper   
å laste opp bilder til en dedikert mappe (S3 bucket folder) for ansiktsgjenkjenning.   
Dette betyr at når et bilde tas fra det private ingangs kamera, lastes det opp til mappen, og   
deretter skannes det for å identifisere om personen er ansatt eller ikke. slik at kunden kan bestemme om døren skal åpnes  
eller ikke.

Den andre funksjonen omfatter både en offentlig inngang og en offentlig utgang.   
Her skannes individene som kommer inn og går ut, slik at selskapet har oversikt over antall personer   
og ansatte til enhver tid i bygningen. Dette gir muligheten til å ha kontroll over hvem som befinner   
seg i lokalene til enhver tid.   
Hvis personen er ansatt å ikke alle rede registrert som i bygget så blir han lagt til hashmappet for ansatte også.

Alle endpoints for bilde scanning har både en manuel api hvor du må selv finne et bilde å laste det opp til rett mappe
og en hvor jeg simulere at bilde blir lastet opp med å plukke et tilfeldig bilde fra camera/pictures og laster det opp til  
gitt plass private, entrance eller exit mappen.   
manuel api må du selv gå å manuelt laste opp et bilde til s3 bucketen og til rett mappe for endpoint du vill teste.

det siste end pointet /get-list-of-persons-in-building
retunere en liste av faceId som er i bygget og om det er ansatt eller ikke.
hvis ingen er i bygget får du tilbake en streng med teksten "building is empty"

### Funcsjonalitet RekognitionController
For å gi bedriftene mer fleksibilitet så har jeg lagt på muligheten til å bestemme hvilken korppsdel den skal scanne for    
værne utstyr. Da kan man sjekke om man har på seg hjelm i somme områder å ansikt beskyttelse i andre

### Måling
For å passe på det økonomiske så setter jeg på en alarm på kostnader her har jeg berre valgt en tilfeldig sum på 1000 per måned
Dette må du da justere inn i forlhold til hva du annser som normal forbruk
Der etter vill jeg sende ut en mail hvis en den private endpointet får 3 scannininger på rad som er uotirisert
vill også lage til måling på hvilken endpoints er i bruk og hvor ofte det blir brukt slik vi bedre kan tilpasse tilbudet vårt.
vill også ha en gauge for hvor mange folk er i bygget til en hver tid å en graph med antall folk i bygget i løpe av en dag
for ppe så vill jeg sende ut alarm om noen går uten PPE
hvis servicen går ned så vill jeg også ha beskjed.

## OPPGAVE 4. Drøfteoppgaver
### A. Kontinuerlig Integrering
Forklar hva kontinuerlig integrasjon (CI) er og diskuter dens betydning i utviklingsprosessen. I ditt svar, vennligst inkluder:

En definisjon av kontinuerlig integrasjon.

I devops så handeler CI om å "merge" koden sin ofte til et sentral repo

Fordelene med å bruke CI i et utviklingsprosjekt - hvordan CI kan forbedre kodekvaliteten og effektivisere utviklingsprosessen.

Siden man "merge" koden sin ofte oppdager man tidlig feil og kan rette feilen med engang å på den måten forbedre kode kvaliteten.
Når man kommiter ofte så blir det konfliktene midre en vhis det går lenge mellom kommits.

Hvordan jobber vi med CI i GitHub rent praktisk? For eskempel i et utviklingsteam på fire/fem utivklere?

Det finnes flere måter å sette opp github på men ofte så vill du at Main ikke skal være låv å kommite direkte til. Så man setter ofte på "branch protection"   
slik at man må bruke pull request fra en annen gren til main. Men man vill at koden skal kommites ofte helst flere ganger om dagen.
så en utvikler vill kanskje lage en gren for en bug fiks, før han pusher den til github   
så merger eller rebase han orgin main inn til sin gren å fikser alle konflikter, deretter pusher han opp til github  
deretter lager han en pull reguest å får den inn i main så lenge den bygger å passere alle tester. deretter sletter sin gren å puller ned den nye koden til sin local main.
Det som er viktig er at man ofte puller fra orgin main enten du jobber i din main eller i en annen gren slik at du minker antall konflikter som må løses.

## B. Sammenligning av Scrum/Smidig og DevOps fra et Utviklers Perspektiv

I denne oppgaven skal du som utvikler reflektere over og sammenligne to sentrale metodikker i moderne programvareutvikling: Scrum/Smidig og DevOps. Målet er å forstå hvordan valg av metodikk kan påvirke kvaliteten og leveransetempoet i utvikling av programvare.
### Scrum/Smidig Metodikk:
Vi gjennomførte nettopp en eksamen i smidig med bruk av scrum hvor vi jobbet mot Munch, det eg la mere til med den framgangsmåten var at det var mye fokus på kunden
og med user story fekk vi veldig tydlig fram hva kunden ville ha å hva vi måtte lage. Mye tid gjekk til møter med sprint plannng, standup, sprint gjenomgang og retoaktiv sprint  
når man hadde startet en sprint å valgt hvilken user sotries som skulle løses så var det lite rom for tilpassing,  
som fungerte greit så lenge alt gjekk etter planen men når ting oppsto oppdaget man hvordan effektivteten ble pårvirket.

### DevOps Metodikk:

    Forklar grunnleggende prinsipper og praksiser i DevOps, spesielt med tanke på integrasjonen av utvikling og drift.
    Analyser hvordan DevOps kan påvirke kvaliteten og leveransetempoet i programvareutvikling.
    Reflekter over styrker og utfordringer knyttet til bruk av DevOps i utviklingsprosjekter.

Devops har 3 grunnleggende prinsipper flyt,Tilbakemelding(feedback) og kontunerlig forbedring
#### Flyt 
hander kontinuerlig integrasjon med git og til dømes github hvor man sørger for at koden blir komittet ofte,   
automatisering gjennom github atction slik man autoatisere bygging, testing og depolyment 

#### Feedback 
handler om telemetri til å måle infrasstructuren din slik du ser om det oppstår problemer med mye trafikk, servicer som er nede osv.
Det er også viktig å følge med på kostnader forbindet med skyløsningen.
Forretningslogikk er også viktig å måle slik man oppdager om endringer man gjorde påvirket til dømes salg på websiden.   
viktig med åvervåking også til dømes med alarmer på mail og tlf. Logging slik man kan spore tilbake til hva som skjdde når det først går gale.
og til slutt så kan man drive med A/B testing, til dømes ved å lage 2 sider for betaling å så splitte kundene mellom de 2 isdene for å se hvem som gjør det best.
 
#### kontunerlig forbedring
handler det om å skape en kulur for deling av erfaring, verktøy, ideer og problem gjenom til dømes "blameless postmortems"  
slik man kan sammen kan forbedrede seg. 

#### Analyse
Med devops så handler det om å gi teamet en større ansvar følese slik at   
man har større eierskap til det man levere som igjen gjør at koden man levere blir bedre
Ved bruk av FLyt så oppdager man feil rask og ting går raskere ved hejlp av automatisering av prosessen.   
Med go Feedback så oppdager man raskt problem som har oppstått i etterkant også.
med desse metodene så kan man raskt fikse opp i problem som oppstår.
Dette gjør at man kan jobbe mye raskere siden alt skjer i ikrement å prosessen er automatisert.
Kontunerlig forbedring hjelper laget å kosntant forbedre seg å bli flinkere som et lag.

### Sammenligning og Kontrast:


## C. Det Andre Prinsippet - Feedback

Tenk deg at du har implementert en ny funksjonalitet i en applikasjon du jobber med. 
Beskriv hvordan du vil etablere og bruke teknikker vi har lært fra "feedback" for 
å sikre at den nye funksjonaliteten møter brukernes behov. Behovene 
Drøft hvordan feedback bidrar til kontinuerlig forbedring og hvordan de kan integreres i ulike stadier av utviklingslivssyklusen.

Siden vi videre utvider appliaksjonen å regner med at antall brukere øker er det viktig med tilbake melding om at applicasjonen kjører slik vi forventer,
dette kan vi gjøre med å lage et dashbord  og legge inn måling i koden for å måle kall på endepunktene ,der vi ser hvor mange request som kommer på det nye endepunktet vs det eksisterende endepunktet.
Er også viktig å vite at appliaksjon ikkje har krejset, dette er viktig å få vite raskt derfor bør man sette opp alarmer som sender ut melding og mail til ansvarlig vakt.
For å finne fram til hva feilen er så er det viktig å også legge inn go logging.
etter hvert som kunde basen øker så er det også viktig å få på plass måling på kostnadene med alarm slik man ikke overstiger budsjettetet.
Med go feedback så oppdager man feil tidlig slik man raskt kan fikse dem, man kan spore bruekre adferden slik man kan tilpasse applikasjon til brukerene, man kan kjøre A/B testing for å finne beste løsningen.
Feedback er noe man bruker gjennom hele prosessen, man starter med det man ser som viktig informasjon som at applikasjonen kjører, alarmer som sender beskjeder og logging slik man kan finne ut av problemet.
etter hvert osm appen er blitt  tatt i bruk så kan man oppdage at man mangler data eller man ser at adferden til brukeren var ikke lik det man trudde å man må derfor få inn bedre målinger.




