# UniTrace: An Object-Oriented Campus Lost and Found Management System

OOP 101 project of Team 9 (Aray Code), IT-2105, Batangas State University. A console-based Java app that logs found items, matches them against lost reports, and requires item-specific proof before release.

## Run

Requires JDK 11 or newer.

    javac -d out src/*.java
    java -cp out Main

Or use `run.sh` (Mac/Linux) / `run.bat` (Windows). Set the `NO_COLOR` environment variable to turn off colored output.

## Demo logins

- Owner: any student number in the format `25-12345`
- Admin / Guard PIN: `1234` (change `ADMIN_PIN` in `ConsoleUI.java`)
- Three sample items are loaded at startup (a student ID, a wallet, an Acer laptop). Laptop proof: serial `ACR-9X2K-7731` or lock screen `sunset beach`.

## Claim flow

1. Owner searches (private details are masked) and submits proof for an item.
2. Correct proof: claim becomes `PENDING_VERIFICATION`; the admin approves it (`READY_FOR_PICKUP`) or rejects it (item returns to `UNCLAIMED`).
3. Three wrong proofs by the same student on the same item lock the claim and send it to manual admin review.
4. The admin releases the item after checking the claimant's school ID; the item becomes `CLAIMED` and a receipt is printed.

## OOP map

| Concept | Where |
|---|---|
| Abstraction | `Item`, `User`, `Claimable`, `ClaimProcessor` |
| Inheritance | `StudentIDCard`, `Electronics`, `PersonalBelonging` extend `Item`; `Student`, `AdminGuard` extend `User` |
| Polymorphism | `verifyClaim()`, `getProofPrompt()`, `getRole()` overrides |
| Encapsulation | private proofs and PIN; `updateStatus()`, `authenticate()` |
| Exceptions | `ItemNotFoundException`, `InvalidClaimException`, `InvalidStatusTransitionException` |
| Collections | `ArrayList`, `HashMap` (failed attempts), `HashSet` (keywords) |

Match score (lost report vs. found item): category 40, keywords 30, location 15, date 10; matches below 55 are hidden.

## Limitations

In-memory storage only (data clears on exit), no SMS/email, text-only "photos", simplified authentication.
