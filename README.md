## 👨‍💼 Employee Management System (EMS)
A full-stack Employee Management System that helps companies manage employee records, authentication, and role-based access.
Built using **Java, Spring Boot, Hibernate, MySQL, Angular, JWT**, and a fully responsive UI.

##  Tech Stack

## Backend

- Java 17+

- Spring Boot

- Hibernate (JPA)

- MySQL 8+

- JWT Authentication

## Frontend

- Angular

- TypeScript

- TailwindCSS

- Chart.js + ng2-charts

## Features

- 🔐 Login/Logout with JWT-based authentication

- 👥 Employee CRUD (add, update, delete, list)

- 🔎 Search & Filter employees

- 🛂 Role-based access (Admin / User)

- 📱 Responsive Angular UI

- 🌐 REST APIs with Spring Boot & Hibernate

- 🗄️ MySQL database integration

- 📦 Demonstrates

## Angular consuming REST APIs

- Spring Boot backend with layered architecture

- Hibernate ORM

- Complete full-stack CRUD operations

- Secure authentication using JWT

| Component           | Port        | Purpose                   |
| ------------------- | ----------- | ------------------------- |
| Spring Boot Backend | **8080**    | REST API & Authentication |
| Angular Frontend    | **4200**    | User Interface            |
| Proxy               | /api → 8080 | Seamless API integration  |

## 🖥️ Backend Setup (ems-backend)

1. Build Project

-  mvn clean install 

2. Run Backend
- mvn spring-boot:run
  
## OR
- ./mvnw spring-boot:run

Backend runs on:
👉 http://localhost:8080

## Frontend Setup (ems-frontend)

1. Install Angular CLI

- npm install -g @angular/cli

2. Install Dependencies

- npm install

3. Update package.json

- "start": "ng serve --proxy-config proxy.conf.json"

4. proxy.conf.json

### libraries
- npm install chart.js
- npm install ng2-charts
- npm install --save-dev @types/jasmine
- npm install -D tailwindcss postcss autoprefixer
- npx tailwindcss init
- npm install --save-dev @angular-devkit/build-angular

5. Run Frontend

- ng serve --proxy-config proxy.conf.json  OR
- npx ng serve --open --proxy-config proxy.conf.json

Frontend runs on:
👉 http://localhost:4200     
<img width="1404" height="879" alt="d491bee9-582e-4e9f-ad1e-645d21d91c11" src="https://github.com/user-attachments/assets/01d9579f-e5ed-46cf-9c1b-b3ebc2ad336c" />
<img width="1906" height="893" alt="b00d1500-16ba-4e4e-b8fc-db340722e322" src="https://github.com/user-attachments/assets/92394860-044a-4690-a138-d91de9005703" />
<img width="1622" height="892" alt="6aa05e80-177c-48bf-a125-f85b91e7b22f" src="https://github.com/user-attachments/assets/e54962c6-a6f9-4055-886a-b22470ad6683" />
<img width="1761" height="886" alt="13dca855-d1f5-4b63-b309-2a78ccbf1ca5" src="https://github.com/user-attachments/assets/2ead4a54-3ca5-4e89-b392-cc19eb036aed" />
<img width="1731" height="885" alt="77ff127c-848e-413a-b56c-de8c6f75e5c7" src="https://github.com/user-attachments/assets/8e0aa671-0311-4e2f-b832-1ae0d845f665" />


