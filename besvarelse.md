# BESVARELSE KANDIDAT 2012 DEVOPS EKSAMEN

## OPPAGAVE 1 A
Jeg valgte å endre navnet på mappen "hello_world" til "ppe_check" for å bedre reflektere hva applikasjonen gjør.
I tillegg endret jeg sluttpunktet fra /hello til /check. 
Det er også gjort andre tilpasninger i template-filen 
for å tydeligere gjenspeile funksjonaliteten.

### For at alt skal fungere med sensor

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
Under "Name" for det første legger du inn "AWS_ACCESS_KEY_ID", og verdien "Access key" fra AWS som "Secret". Deretter trykker du på "Add secret". Deretter oppretter du en til med "AWS_SECRET_ACCESS_KEY" som "Name", og "Secret access key" fra AWS som "Secret". Se
![github_secret.png](..%2F..%2F..%2FUsers%2Fbjart%2FDesktop%2Fgithub_secret.png)

### Code

For å teste workflow på hovedgrenen og lage sin egen SAM, må sensoren endre filen .github/workflows/kjellMainBranch.yml og justere følgende miljøvariabler:  
S3_IMAGE_BUCKET: til ønsket bucket for å sjekke bilder.  
STACK_NAME: for å endre hva SAM(Lamda funksjonen) skal hete  

Det er ikke nødvendig å gjøre endringer i workflows-filene for å teste workflow mot andre grener. For å teste, trenger man kun å opprette en ny gren og pushe den til GitHub.

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

