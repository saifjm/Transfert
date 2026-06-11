# MS-TR - Transferts emis vers l'etranger

## 1. Presentation

**MS-TR** est le microservice metier dedie a la gestion des transferts emis vers l'etranger dans IBANSYS. Son perimetre couvre l'ensemble des operations de transfert, qu'elles soient **commerciales** ou **financieres**.

Le module ne se limite donc pas aux transferts commerciaux adosses a un TCE. Les transferts import adosses a un TCE constituent un cas d'usage important, mais ils s'inscrivent dans un perimetre plus large incluant les transferts financiers, les operations courantes, les reglements lies aux services, revenus, frais, financements, investissements, operations diverses et autres motifs autorises selon la reglementation applicable.

MS-TR s'inscrit dans la trajectoire de modernisation d'IBANSYS et vise a remplacer progressivement les traitements monolithiques par un socle Spring Boot plus modulaire, auditable et integrable avec les autres composants du systeme d'information bancaire.

## 2. Objectifs du projet

Le projet a pour objectifs principaux :

- gerer les transferts emis vers l'etranger, commerciaux et financiers ;
- couvrir les cas d'usage adosses a un TCE sans limiter le module a ce seul perimetre ;
- assurer la generation d'une reference unique d'operation ;
- piloter le cycle de vie metier de l'operation ;
- separer les operations en cours dans une table de mouvement et les operations finalisees dans une table definitive ;
- structurer les donnees de l'ordre client, les modalites de paiement, les donnees reglementaires et les donnees interbancaires ;
- preparer la generation de messages ISO 20022 / SWIFT+ ;
- assurer la tracabilite complete des decisions, controles, anomalies et validations ;
- faciliter l'integration avec les services de workflow, comptabilite, commission, reporting BCT et messagerie bancaire.

## 3. Positionnement fonctionnel

MS-TR couvre plusieurs familles de transferts emis :

| Famille | Description | Exemple |
|---|---|---|
| Transferts commerciaux | Reglements lies au commerce exterieur, import/export, titres de commerce exterieur | Paiement fournisseur etranger adosse a un TCE |
| Transferts financiers | Flux financiers autorises hors achat de marchandises | Revenus, remboursements, investissements, financements |
| Services et operations courantes | Reglements de prestations, frais, abonnements, redevances, honoraires | Paiement de service a l'etranger |
| Frais et revenus | Frais bancaires, interets, dividendes, loyers, commissions | Paiement d'interets ou de commissions |
| Operations diverses | Operations autorisees selon la reglementation des changes et le referentiel BCT | Autres transferts justifies |

## 4. Perimetre fonctionnel initial

Le Sprint 1 couvre le socle generique du module MS-TR :

| Reference | Domaine | Description |
|---|---|---|
| PB-01 | Initialisation | Acces au module des transferts emis pour les utilisateurs habilites |
| PB-02 | Initialisation | Generation automatique d'une reference unique d'operation |
| PB-03 | Cycle de vie | Gestion des statuts de l'operation |
| PB-37 | Habilitations | Controle d'acces par role |
| PB-38 | Integration | Definition des contrats API et evenements interservices |

Les cas commerciaux adosses a un TCE sont traites comme une specialisation metier du module, mais le socle reste generique pour tous les transferts emis.

Les statuts metier retenus sont :

- `SAISI`
- `A_COMPLETER`
- `EN_CONTROLE`
- `EN_VALIDATION`
- `VALIDEE`
- `REJETEE`
- `EN_EXCEPTION`
- `ANOMALIE_OUTPUT`
- `ANNULEE`
- `CLOTUREE`

## 5. Architecture fonctionnelle

Le projet est structure autour des briques metier suivantes :

1. **Initialisation de l'operation**  
   Creation d'une operation de transfert avec reference unique, statut initial, famille de transfert et canal d'origine.

2. **Donnees de l'ordre client**  
   Donneur d'ordre, beneficiaire, banque beneficiaire, montant, devise, motif, references et justificatifs.

3. **Typologie et donnees reglementaires**  
   Nature de l'operation, famille commerciale ou financiere, code balance des paiements, pays, devise, support reglementaire, autorisation ou titre lorsque requis.

4. **Modalites de paiement**  
   Source des fonds, comptes debites, achat devise, cours normal ou negocie, achat a terme, financement, frais, date valeur et couverture.

5. **Controle et validation**  
   Verification des donnees obligatoires, disponibilite des fonds, coherence reglementaire, regles metier et habilitations.

6. **Generation output**  
   Preparation des messages SWIFT / ISO 20022 et des evenements vers les composants aval.

7. **Audit et tracabilite**  
   Conservation des snapshots, controles, decisions, statuts et evenements d'integration.

## 6. Architecture technique cible

Le microservice est concu selon une architecture Spring Boot en couches :

```text
com.smi.mstr.transfer
|-- api              # Controllers REST
|-- application      # Services applicatifs et orchestration metier
|-- domain           # Entites, agregats, enums et logique de domaine
|-- dto              # Objets d'entree/sortie API
|-- mapper           # Mapping DTO <-> Domaine
|-- repository       # Acces aux donnees JPA
|-- integration      # Clients externes, Kafka, workflow, SWIFT+, reporting
`-- shared           # Exceptions, utilitaires, constantes et audit
```

## 7. Modele de persistance

Le principe retenu est une separation entre :

- une table de mouvement pour les operations en cours ;
- une table definitive pour les operations finalisees.

Exemple :

```text
MVT_TR_OPERATION  -> operation en cours de saisie, controle, validation ou correction
TR_OPERATION      -> operation finalisee, validee et historisee
```

Cette approche permet de mieux controler le cycle de vie, d'isoler les brouillons et anomalies, et de ne publier vers les traitements aval que les operations finalisees.

## 8. Composant Modalites de paiement

Une operation de transfert possede une seule modalite de paiement active. Cette modalite peut cependant contenir plusieurs sources de fonds, comptes debites, frais, supports et controles.

Cardinalite recommandee :

```text
TRANSFER_ORDER 1 --- 0..1 active PAYMENT_MODALITY
PAYMENT_MODALITY 1 --- 1..n FUND_SOURCE
PAYMENT_MODALITY 1 --- 0..n DEBIT_ACCOUNT
PAYMENT_MODALITY 1 --- 0..n CHARGE
PAYMENT_MODALITY 1 --- 0..n SUPPORT
```

La modalite de paiement couvre :

- compte TND debite ;
- compte devise debite ;
- achat devise au cours normal ;
- achat devise au cours negocie ;
- achat a terme ;
- arbitrage devise/devise ;
- fonds recus d'une autre banque ;
- financement en devise ;
- frais, commissions et taxes ;
- justificatifs et supports reglementaires.

## 9. Integration ISO 20022 / SWIFT+

Le modele est prepare pour alimenter la generation de messages ISO 20022, notamment :

- `pacs.008` pour le transfert client ;
- `pacs.009 COV` lorsque la couverture interbancaire est necessaire ;
- mappings possibles vers les formats MT legacy, notamment MT103.

Les structures metier internes restent canoniques et ne dependent pas directement du XML ISO. Le mapping vers ISO 20022 est realise dans une couche dediee afin de conserver l'independance du domaine metier.

## 10. Prerequis techniques

- Java 17 ou superieur
- Spring Boot 3.x
- Maven 3.9+
- Oracle Database
- Git
- IDE recommande : IntelliJ IDEA ou VS Code

## 11. Installation locale

Cloner le depot :

```bash
git clone <repository-url>
cd ms-tr
```

Compiler le projet :

```bash
mvn clean install
```

Lancer l'application :

```bash
mvn spring-boot:run
```

## 12. Configuration

La configuration applicative est portee par `application.properties` ou `application.yml`.

Exemple minimal :

```properties
spring.application.name=ms-tr
server.port=8080

spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/ORCLPDB1
spring.datasource.username=TR_USER
spring.datasource.password=TR_PASSWORD
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect
```

Les valeurs sensibles ne doivent pas etre versionnees dans Git. Utiliser des variables d'environnement ou une configuration externe pour les environnements de test, recette et production.

## 13. Initialisation Git

Pour initialiser le projet localement :

```bash
git init
git add .
git commit -m "Initial commit - MS-TR project skeleton"
git branch -M main
git remote add origin <repository-url>
git push -u origin main
```

## 14. Convention de branches

Convention recommandee :

```text
main                 # branche stable
release/<version>    # preparation livraison
feature/<code-pb>    # developpement d'une user story
fix/<description>    # correction anomalie
hotfix/<description> # correction urgente
```

Exemples :

```bash
git checkout -b feature/pb-01-module-access
git checkout -b feature/pb-02-operation-reference
git checkout -b feature/pb-03-status-lifecycle
```
