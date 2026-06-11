# Description du projet - MS-TR

## Nom du projet

**MS-TR - Microservice de gestion des transferts emis vers l'etranger**

## Description courte

MS-TR est un microservice Spring Boot destine a la gestion des transferts emis vers l'etranger. Il couvre les operations de transfert **commerciales et financieres** : transferts import adosses a un TCE, reglements de services, revenus, frais, financements, investissements, operations courantes et autres transferts autorises selon la reglementation applicable.

## Description detaillee

Le projet MS-TR s'inscrit dans la modernisation des solutions bancaires de SMI, notamment autour des paiements internationaux, du commerce exterieur, des operations financieres et de la conformite reglementaire.

Contrairement a une vision limitee aux transferts commerciaux adosses a un TCE, MS-TR constitue le socle general des transferts emis vers l'etranger. Le cas commercial adosse a un TCE reste une specialisation fonctionnelle importante, mais il ne definit pas a lui seul le perimetre du module.

L'application gere une operation de transfert depuis sa creation par un utilisateur habilite jusqu'a sa validation et sa finalisation. Tant que l'operation est en cours de traitement, elle est conservee dans une table de mouvement. Une fois validee et finalisee, elle est historisee dans une table definitive.

Le projet est concu pour s'integrer avec les composants du systeme d'information bancaire : workflow de validation, controle de disponibilite des fonds, change, commissions, comptabilite, reporting BCT et plateforme de messagerie bancaire SWIFT+.

## Objectifs metier

- Gerer les transferts emis vers l'etranger, commerciaux et financiers.
- Couvrir les transferts import adosses a un TCE comme cas d'usage specifique.
- Encadrer la saisie de l'ordre client.
- Controler les donnees du donneur d'ordre, du beneficiaire et de la banque beneficiaire.
- Identifier la typologie de transfert : commercial, financier, service, revenu, frais, financement, investissement ou autre operation autorisee.
- Definir les modalites de paiement : compte debite, achat devise, change negocie, achat a terme, financement ou fonds recus.
- Controler la disponibilite et la securisation des fonds.
- Alimenter les traitements de generation SWIFT / ISO 20022.
- Assurer une tracabilite complete des operations, statuts, controles et decisions.

## Objectifs techniques

- Construire un microservice Spring Boot modulaire.
- Appliquer une architecture en couches : API, application, domaine, repository, integration.
- Separer les operations en cours des operations finalisees.
- Preparer un modele canonique independant des formats ISO 20022.
- Faciliter les integrations REST et evenementielles.
- Assurer une base solide pour les prochains sprints fonctionnels.

## Perimetre initial

Le premier perimetre fonctionnel inclut le socle generique MS-TR :

- acces au module par utilisateur habilite ;
- creation d'une operation de transfert ;
- generation automatique d'une reference unique ;
- consultation des operations ;
- gestion du cycle de vie par statuts ;
- classification de l'operation par famille de transfert ;
- preparation des contrats API et evenements interservices.

## Technologies ciblees

- Java 17+
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Oracle Database
- Maven
- Git
- Kafka ou REST pour les integrations interservices
- ISO 20022 / SWIFT+ pour la messagerie bancaire

## Statut du projet

Projet en phase d'initialisation et de cadrage technique. Le backlog de Sprint 1 est identifie et les premieres structures de domaine, DTO, API et services doivent etre mises en place sur un socle generique couvrant tous les transferts emis vers l'etranger.
