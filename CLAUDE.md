# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Sponsorenlauf** is a desktop Java/Swing application for managing a school charity run (Sponsorenlauf) at Rudolf Steiner Schule St. Gallen. It manages classes, students, sponsors, and sponsorships, and handles billing via PDF generation and email dispatch.

## Build & Run

This is a Maven project targeting Java 19.

```bash
# Build (produces target/Sponsorenlauf-1.0-SNAPSHOT.jar)
mvn package

# Run (main class)
# ch.steinerschule_stgallen.model.Main
```

The project is configured for IntelliJ IDEA artifact packaging (see `.idea/artifacts/`). There are no automated tests.

## Email Sending (Microsoft Graph API)

Email dispatch requires three environment variables at runtime:
```
CLIENT_ID=<azure-app-client-id>
CLIENT_SECRET=<azure-app-client-secret>
TENANT_ID=<azure-tenant-id>
```
Without these, `EmailService` throws `NoKeysException` and all emails will silently fail (recorded in the failed-to-send list). Emails are sent from `sponsorenlauf@steinerschule-stgallen.ch`.

## Architecture

### MVC Structure

```
model/      — Data layer
views/      — Swing UI
controller/ — Event routing between view and model
billing/    — PDF, email, and QR bill generation
util/       — Enums (Action, ListType, SponsorshipType, Currency, EmailType) and CONSTANTS
```

### Data Model

The core hierarchy: `Model` → `StudentClass` → `Student` → `Sponsorship` ↔ `Sponsor`

- `Model` owns `LinkedList<StudentClass>` and `LinkedList<Sponsor>` (these are the roots serialized to JSON).
- `Sponsorship` is a many-to-many link between `Student` and `Sponsor`. Both sides hold a list of `Sponsorship` objects.
- `SponsorshipType`: `PER_LAP` (multiplied by student's lap count) or `ONCE_OFF`.
- `Currency`: `CHF` or `EUR`. EUR→CHF conversion uses `CONSTANTS.exchangeRate` (currently `0.97`).

### JSON Persistence (Jackson)

- `Model.testSave()` / `Model.testLoad()` serialize/deserialize to `.json` files.
- `Sponsor` uses `@JsonIdentityInfo` to handle circular references via an integer `id` property.
- After deserialization, `LoadedStructure.connectSponsorships()` rebuilds the bidirectional object graph using the static maps `LoadedStructure.idToStudent` and `LoadedStructure.idToSponsor`.
- **Critical**: `Model.getAllClasses()` and `Model.getAllSponsors()` must never be deleted or annotated with `@JsonIgnore` — Jackson needs them for serialization.

### Global ID / Bill Number

`Main.getNextId()` and `Main.updateMinId()` manage a global auto-increment counter used as both the entity ID (for JSON identity resolution) and the bill number shown to sponsors.

### Billing Pipeline

1. **HTML templates** in `src/main/resources/` use `[placeholder]` syntax (e.g., `[title]`, `[name]`, `[entries]`, `[total]`, `[billNum]`, `[year]`, `[qrcodes]`).
2. Templates per document type and delivery method:
   - Bill: `emailTemplate.html` (email body) + `emailTemplatePDF.html` (PDF attachment)
   - Reminder: `reminderEmailTemplate.html` + `reminderEmailTemplatePDF.html`
   - Thank-you: `thanksTemplate.html` + `thanksTemplatePDF.html`
3. `PDFCreator.convertHTMLtoPDF()` converts HTML→PDF (iText html2pdf), then stamps every page with `template.pdf` via `PDFCreator.stampPDFwithHead()`. **Note**: `template.pdf` is read from the working directory as a relative path (`new PdfReader("template.pdf")`), not from the classpath.
4. `QRBillCreator` generates Swiss QR payment slip images/PDFs and these are appended to PDFs or embedded in emails as inline images (`cid:qrcodeCHF`, `cid:qrcodeEUR`).
5. `EmailSenderWorker` (a `SwingWorker`) sends emails off the EDT and publishes progress to a `LoadingBar` dialog.

### View Structure

`View` is the main Swing frame with three tabs:
- **Dateneingabe** — four `ListView` columns (classes, students, sponsorships, sponsors) for data entry.
- **Datenauswertung** — `AnalysisView` showing statistics and rankings.
- **Rechnungen versenden** — `SendBillsView` with checkbox-style sponsor selection and actions for email dispatch, PDF export, and status management.

`Controller` implements `ListController` and routes button presses (`Action`: ADD/EDIT/DELETE) and list selection events (`ListType`: CLASSES/STUDENTS/SPONSORSHIP/SPONSORS) from the view to the model.

### Search/Filter

`Model` maintains parallel `searchRestricted*` lists filtered by query strings set via `setClassQuery()`, `setStudentQuery()`, etc. The UI always displays the filtered lists, while selection indexes map into these filtered lists (not the full lists).
