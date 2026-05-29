# MovieList

Application web de gestion de vos films film préférés. Recherchez des films via l'API TMDB, constituez votre liste "à voir" et notez ceux que vous avez regardés.

## Stack

- **Java 21** / Spring Boot 3.3
- **PostgreSQL**
- **Docker / Docker Compose** : deux services avec `auth-service` (port 8081) et `app-service` (port 8080)
- **Thymeleaf + Tailwind CSS**
- **TMDB API**

## Prérequis

- Docker Desktop
- Une clé API TMDB gratuite : [themoviedb.org](https://www.themoviedb.org/settings/api)

## Lancer en local

```bash
cp .env.example .env

docker-compose up --build
```

L'application est accessible sur `http://localhost:8080`.

## CI & Tests

Une pipeline GitHub Actions tourne à chaque push : 
 - build Maven
 - tests unitaires et tests web sur les deux services, avec rapport de couverture JaCoCo.

| Service | Couverture |
|---|---|
| auth-service | 97% |
| app-service | 88% |
