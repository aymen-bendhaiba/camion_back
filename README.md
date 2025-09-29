# Backend - Spring Boot (compatible avec le frontend Flutter)

## Prérequis
- Java 17+
- Maven

## Lancer le backend

mvn spring-boot:run

Port par défaut: 8082 (le frontend utilise `http://<ip>:8082`).

Assurez-vous d'activer la génération automatique du schéma (si besoin) dans `application.properties`:

spring.jpa.hibernate.ddl-auto=update

## Authentification
- JWT via `/login` (existant)
- Toutes les routes `/api/**` nécessitent `Authorization: Bearer <token>`

## WebSockets
- ws://<host>:8082/ws/camions
- ws://<host>:8082/ws/chat?userId=<id>&token=<jwt>

## Endpoints exposés

- Profil
  - GET `/profil`
  - PUT `/update-profile`

- Réservations (`/api/reservations`)
  - POST `/` créer une réservation
  - GET `/` réservations de l'utilisateur (chargeur)
  - GET `/status/{status}` réservations de l'utilisateur par statut
  - PUT `/{reservationId}/camion/{camionId}/{isIgnore}` assigner camion (chargeur)
  - GET `/available` réservations disponibles (camion non assigné)
  - GET `/available/status/{status}` disponibles par statut
  - GET `/all/status/{status}` toutes par statut
  - GET `/my` réservations du transporteur connecté
  - GET `/my/status/{status}` réservations du transporteur par statut
  - GET `/my/{status}/with-chargeur` réservations + info chargeur
  - PUT `/{reservationId}/status?status=...` mise à jour du statut (transporteur)
  - GET `/recapitulatif` récap documents (chargeur/transporteur)
  - GET `/my/statistics` stats transporteur

- Camions (`/api/camions`)
  - POST `/` créer camion
  - GET `/` liste camions
  - GET `/proches?latitude=&longitude=&rayonKm=`
  - GET `/transporteur/{transporteurId}` camion par transporteur
  - GET `/my` camion du transporteur connecté
  - GET `/{id}` camion par id
  - PUT `/{id}` mise à jour camion
  - PUT `/{id}/position` mise à jour position (tracking)
  - POST `/{id}/simulate-movement` simulation (public)

- Messages (`/api/messages`)
  - POST `/send` envoyer un message
  - GET `/reservation/{reservationId}` messages d'une réservation
  - PUT `/reservation/{reservationId}/read` marquer lus
  - GET `/unread/count` compteur non lus

- Propositions (`/api/proposals`)
  - POST `/{reservationId}` créer une proposition (transporteur) body: `{ prix?: number, commentaire?: string }`
  - GET `/{reservationId}` lister les propositions pour une réservation
  - POST `/{reservationId}/select/{proposalId}` sélectionner une proposition (chargeur)

- Dashboard (`/api/dashboard`)
  - GET `/stats`

- Admin (`/api/admin`)
  - GET `/pending-validations` éléments en attente (proposition sélectionnée non validée)
  - POST `/reservations/{reservationId}/validate` valider

## Fichiers ajoutés
- `entities/Proposal.java`
- `repository/ProposalRepository.java`
- `services/ProposalService.java`, `services/ProposalServiceImpl.java`
- `controller/ProposalController.java`
- `controller/AdminController.java`

## Notes d'intégration
- Les routes et structures de réponse correspondent aux appels utilisés par le frontend (Dio/WebSocket).
- Le champ `selectedPrice` est renvoyé pour l'écran Admin.
- Les WebSockets émettent les mises à jour camions et notifications de nouveaux messages.
