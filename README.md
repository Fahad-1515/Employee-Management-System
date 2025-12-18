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

<img width="1615" height="871" alt="Screenshot 2025-12-18 192458" src="https://github.com/user-attachments/assets/6ea7562d-d293-475b-823a-f22480198fb6" />
<img width="1875" height="899" alt="Screenshot 2025-12-18 192529" src="https://github.com/user-attachments/assets/997a95b9-116a-47e6-9662-feace9238d57" />
<img width="1873" height="904" alt="Screenshot 2025-12-18 192538" src="https://github.com/user-attachments/assets/6713b754-f25f-48ad-872c-2f35b95d7a2a" />
<img width="1858" height="893" alt="Screenshot 2025-12-18 192837" src="https://github.com/user-attachments/assets/5d87d159-fa45-4691-b43a-6146dc5db032" />


