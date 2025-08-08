# Loan Calculator API

API RESTFul para calcular prestações de empréstimos e simular cenários de financiamento

## Como executar

**Pré-requisitos:**
- Java 21+
- Maven 3.6+

**Execute a aplicação:**
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows (PowerShell)
./mvnw.cmd spring-boot:run

# Windows (Command Prompt)
mvnw.cmd spring-boot:run
```

## Endpoints

**Documentação completa disponível em:** http://localhost:8080/swagger-ui.html

### Loan Calculator

| Método | Endpoint              | Descrição                                     |
|--------|-----------------------|-----------------------------------------------|
| POST | `/api/loan-calculate` | Gerar relatório com parcelas do financiamento |

## Tecnologias

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Data JPA**
- **H2 Database**
- **Maven**
- **JUnit 5 + Mockito**
- **Lombok**
- **Bean Validation**
- **SpringDoc OpenAPI**

## Implementações Técnicas

### Arquitetura

A arquitetura segue o padrao **Layered Architecture** (Controller -> Service -> Repository)
```
src/main/java/com.github.xltgui/loancalculatorchallenge/
├── api/                    # 📄 Data Transfer Objects (DTOs)
│   ├── common/             # DTOs comums para tratamento de exception
│   │   ├── CustomFieldError.java
│   │   └── ResponseError.java
│   ├── LoanRequest.java   
│   └── PaymentDetailResponse.java
├── config/                 # ⚙️ Configurações da API
│   └── OpenApiConfiguration.java
├── exception/              # ⛔ Gerenciamento customizado de exceções
│   └── InvalidDateDateException.java
├── model/                  # 🎯 Regra de negócio principal e entidades
│   ├── Loan.java     
│   ├── LoanCalculationService.java 
│   ├── LoanCalculatorValidator.java  
│   └── PaymentDetail.java
├── repository/             # 💾 Interface de acesso a dados
│   ├── LoanRepository.java
│   └── PaymentDetailRepository.java
└── web/                    # 🌐 REST Controllers e entry points
    ├── common/             # Components comums da camada web
    │   └── GlobalExceptionHandler.java
    ├── LoanCalculatorController.java
    ├── LoanCalculatorMapper.java
    └── Application.java
```

### Mapeamento de Objetos
* Mappers manuais   (`LoanCalculatorMapper`)
* Records utilizados para DTOs
  * Aproveitando um dos benefícios do Java 21.
  * Reduzir código boilerplate.
  * Ter um objeto imutável.

### Banco de Dados
* H2 Database
  * Dados sendo criados e salvos somente em tempo de execução.
* Obs: Considero migrar para **PostgreSQL** com docker em implementações futuras.

### Documentação

* SpringDoc OpenAPI
  * Interface visual com **Swagger UI**.
  * Facilidade de consumo pelos usuários da API.

### Testes
* Aquitetura
```
src/test/java/com.github.xltgui/loancalculatorchallenge/
  ├── integration/              # 🧪 Testes de integração
  │   └── LoanCalculatorIntegrationTest.java
  └── unit/                     # 🔬 Testes unitários
  ├── controller/       
  │   └── LoanCalculatorControllerUnitTest.java
  └── ApplicationTests.java
```

* Execute os testes:
```bash
# Linux/Mac
./mvnw test

# Windows (PowerShell)
./mvnw.cmd test

# Ou com Maven instalado (todas as plataformas)
mvn test
```



