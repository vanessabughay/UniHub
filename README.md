# UniHub - Guia de Execução

Este guia resume o que você precisa instalar e quais comandos rodar para subir o backend, banco e aplicativo Android.

## Pré-requisitos
- **Docker Desktop**: necessário para subir o banco PostgreSQL e o backend via Docker Compose.
- **Android Studio** (com SDK e emulador ou dispositivo físico configurado): para abrir o projeto Android localizado em [`UniHub/`](UniHub/).
- **Git** (opcional): para clonar o repositório.

## Backend e banco de dados (Docker)
1. Certifique-se de que o Docker Desktop esteja em execução.
2. Na raiz do projeto, execute:
   ```bash
   docker compose up --build
   ```
   - Este comando cria um contêiner PostgreSQL (`unihub_db`) na porta `5437` e o backend Spring Boot (`spring_backend`) na porta `8080`, conforme definido em [`docker-compose.yml`](docker-compose.yml).
3. Para parar os serviços, use:
   ```bash
   docker compose down
   ```

> Se preferir rodar o backend fora do Docker, você pode usar `./gradlew bootRun` dentro de [`backend/`](backend/), ajustando as credenciais do banco conforme necessário.

## Ajuste de IP no backend
- O Android (especialmente em dispositivos físicos) precisa alcançar o backend pela rede Wi‑Fi. Edite [UniHub/local.properties] para apontar para o IP da sua máquina na rede, por exemplo:
  ```properties
  EX: INSTITUICAO_BASE_URL=http://192.168.1.2:8080/  INSTITUICAO_BASE_URL=http://(Seu IP local):8080/
  ```
  Substitua `SEU_IP_LOCAL` pelo endereço IPv4 do computador que está rodando o Docker. Garanta que o celular e o servidor estejam conectados **à mesma rede Wi‑Fi**.

## Frontend (aplicativo Android)
1. Abra o projeto em `UniHub/` pelo Android Studio.
2. Verifique a **Base URL** do Retrofit em [`UniHub/app/src/main/java/com/example/unihub/data/config/RetrofitClient.kt`](UniHub/app/src/main/java/com/example/unihub/data/config/RetrofitClient.kt). Para emuladores Android Studio, o valor padrão `http://10.0.2.2:8080/` funciona e caso use celular físico, sistema já está configurado.
3. Conecte o dispositivo (na mesma rede do servidor) ou inicie um emulador.
4. Faça o build/instalação pelo Android Studio (`Run > Run 'app'`).

## Resumo rápido de comandos
- Subir backend + banco: `docker compose up --build`
- Derrubar serviços: `docker compose down`
- Rodar backend localmente (fora do Docker): `cd backend && ./gradlew bootRun`

Seguindo estes passos, o backend, banco de dados e aplicativo Android devem comunicar-se corretamente na mesma rede.