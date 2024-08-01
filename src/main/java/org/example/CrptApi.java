package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.io.StringWriter;
import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class CrptApi {

    private final HttpClient httpClient;
    private final Semaphore semaphore;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final long timeLimitInMillis;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newBuilder()
                .executor(Executors.newFixedThreadPool(10))
                .build();
        this.semaphore = new Semaphore(requestLimit);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.executorService = Executors.newSingleThreadExecutor();
        this.timeLimitInMillis = timeUnit.toMillis(1);
    }

    public void createDocument(Document document, String signature) throws InterruptedException, IOException {
        semaphore.acquire();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                    .header("Content-Type", "application/json")
                    .header("Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(convertDocumentToJson(document)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            throw e;
        } finally {
            scheduler.schedule(() -> semaphore.release(), timeLimitInMillis, TimeUnit.MILLISECONDS);
        }
    }

    private String convertDocumentToJson(Document document) {
        JsonObjectBuilder docBuilder = Json.createObjectBuilder()
                .add("doc_id", nonNull(document.getDocId()))
                .add("doc_status", nonNull(document.getDocStatus()))
                .add("doc_type", nonNull(document.getDocType()))
                .add("importRequest", document.isImportRequest())
                .add("owner_inn", nonNull(document.getOwnerInn()))
                .add("participant_inn", nonNull(document.getParticipantInn()))
                .add("producer_inn", nonNull(document.getProducerInn()))
                .add("production_date", nonNull(document.getProductionDate()))
                .add("production_type", nonNull(document.getProductionType()))
                .add("reg_date", nonNull(document.getRegDate()))
                .add("reg_number", nonNull(document.getRegNumber()));

        if (document.getProducts() != null) {
            JsonArrayBuilder productsArrayBuilder = Json.createArrayBuilder();
            for (Document.Product product : document.getProducts()) {
                JsonObjectBuilder productBuilder = Json.createObjectBuilder()
                        .add("certificate_document", nonNull(product.getCertificateDocument()))
                        .add("certificate_document_date", nonNull(product.getCertificateDocumentDate()))
                        .add("certificate_document_number", nonNull(product.getCertificateDocumentNumber()))
                        .add("owner_inn", nonNull(product.getOwnerInn()))
                        .add("producer_inn", nonNull(product.getProducerInn()))
                        .add("production_date", nonNull(product.getProductionDate()))
                        .add("tnved_code", nonNull(product.getTnvedCode()))
                        .add("uit_code", nonNull(product.getUitCode()))
                        .add("uitu_code", nonNull(product.getUituCode()));

                productsArrayBuilder.add(productBuilder);
            }
            docBuilder.add("products", productsArrayBuilder);
        }

        return docBuilder.build().toString();
    }

    private String nonNull(String value) {
        return value != null ? value : "";
    }

    public static class Document {
        // Поля, геттеры и сеттеры для представления структуры документа JSON
        private String participantInn;
        private String docId;
        private String docStatus;
        private String docType;
        private boolean importRequest;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String productionType;
        private String regDate;
        private String regNumber;
        private Product[] products;

        // Геттеры и сеттеры для всех полей класса Document

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        public Product[] getProducts() {
            return products;
        }

        public void setProducts(Product[] products) {
            this.products = products;
        }

        public static class Product {
            private String certificateDocument;
            private String certificateDocumentDate;
            private String certificateDocumentNumber;
            private String ownerInn;
            private String producerInn;
            private String productionDate;
            private String tnvedCode;
            private String uitCode;
            private String uituCode;

            public String getCertificateDocument() {
                return certificateDocument;
            }

            public void setCertificateDocument(String certificateDocument) {
                this.certificateDocument = certificateDocument;
            }

            public String getCertificateDocumentDate() {
                return certificateDocumentDate;
            }

            public void setCertificateDocumentDate(String certificateDocumentDate) {
                this.certificateDocumentDate = certificateDocumentDate;
            }

            public String getCertificateDocumentNumber() {
                return certificateDocumentNumber;
            }

            public void setCertificateDocumentNumber(String certificateDocumentNumber) {
                this.certificateDocumentNumber = certificateDocumentNumber;
            }

            public String getOwnerInn() {
                return ownerInn;
            }

            public void setOwnerInn(String ownerInn) {
                this.ownerInn = ownerInn;
            }

            public String getProducerInn() {
                return producerInn;
            }

            public void setProducerInn(String producerInn) {
                this.producerInn = producerInn;
            }

            public String getProductionDate() {
                return productionDate;
            }

            public void setProductionDate(String productionDate) {
                this.productionDate = productionDate;
            }

            public String getTnvedCode() {
                return tnvedCode;
            }

            public void setTnvedCode(String tnvedCode) {
                this.tnvedCode = tnvedCode;
            }

            public String getUitCode() {
                return uitCode;
            }

            public void setUitCode(String uitCode) {
                this.uitCode = uitCode;
            }

            public String getUituCode() {
                return uituCode;
            }

            public void setUituCode(String uituCode) {
                this.uituCode = uituCode;
            }
        }
    }

    public static void main(String[] args) {
        try {
            CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);
            Document doc = new Document();
            api.createDocument(doc, "your-signature-here");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
