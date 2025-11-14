# Loan Calculator API

API para cálculo de empréstimos, construída em **Java 21** com **Spring Boot**.  
A documentação interativa da API está disponível via **Swagger/OpenAPI**.

---

## Tecnologias Utilizadas

- Java 21
- Spring Boot 3.2+
- Springdoc OpenAPI (Swagger)
- Maven
- Tomcat Embedded
- BigDecimal para cálculos financeiros precisos

---

## Rodando a aplicação localmente

Siga os passos abaixo para rodar a API na sua máquina:

1. Clone o repositório:
   ```bash
   git clone https://github.com/yurijatki09/api-loan-calculator.git

2. Entre na pasta do projeto
  cd api-loan-calculator/loan-calculator

3. Rode a aplicação usando Maven:
   ./mvnw spring-boot:run
   
4. Abra a documentação interativa da API no navegador:
   http://localhost:8080/swagger-ui/index.html

## Endpoints Principais
| Método | Endpoint    | Descrição                  |
| ------ | ----------- | -------------------------- |
| GET    | /loans      | Lista todos os empréstimos |
| POST   | /loans      | Cria um novo empréstimo    |
| GET    | /loans/{id} | Busca empréstimo por ID    |
Para a documentação completa, utilize o Swagger UI: http://localhost:8080/swagger-ui/index.html

## Estrutura de Cálculo

Utiliza BigDecimal para precisão financeira

Base de dias definida via application.yml (ex.: 360 dias)

Calcula juros diários entre datas usando ChronoUnit.DAYS
