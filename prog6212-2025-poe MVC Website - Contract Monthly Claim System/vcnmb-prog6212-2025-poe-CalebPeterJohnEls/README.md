# Caleb Peter-John Els
# PROG 2B POE PART 1
# Contract Monthly Claim System (CMCS)

## QUESTION 1 : Documentation

### 1.1 Design Choices
**Roles:**
- Lecturer can submit and track claims.
- Programme Coordinator / Academic Manager can review, approve, or reject claims, as well as leave a comment if they choose to.
- Separate roles ensure clear responsibility and reduce errors.

**Web-based solution using ASP.NET Core MVC:**
- Strong support for Model-View-Controller (MVC) architecture.
- Scalability and integration with Azure Blob Storage for file handling.
- Strong tooling for C# development.

**Automated calculations:**
- Claim amounts are automatically calculated according to the lecturer's preset hourly rate and the number of hours performed in order to reduce user errors.

**Claim Status:**
- A record of each claim’s status is kept for accountability.

**Document upload:**
- Supporting documents such as signed timesheets, proof of hours worked or any other supporting documents can be uploaded securely.

**User-centric interface:**
- The system layout prioritizes a simple, user-friendly design, so users can complete tasks with minimal training and in a short amount of time.

(Satzinger, Jackson and Burd, 2016)
---

### 1.2 Database Structure

**Users**
- **Entities:**  
  UserID (PK), FullName, Email, Role, HourlyRate, Password
- **Purpose:**  
  Stores user information and role for authentication and claim calculations.

**Claim:**
- **Entities:**  
  ClaimID (PK), UserID (FK), StartDate, EndDate, TotalHours, TotalAmount, StatusID (Pending, Approved, Not Approved) StatusID, Attachment (ID), SubmitDate
- **Purpose:**  
  Stores details of each claim submission.

**Attachment:**
- **Entities:**  
  AttachmentID (PK), ClaimID (FK), FileName, FilePath
- **Purpose:**  
  Manages uploaded supporting documents linked to claims.

**Approvals:**
- **Entities:**  
  ApprovalID (PK), ClaimID (FK), Comment, ApprovalDate, StatusID(FK)
- **Purpose:**  
  Tracks approval actions and comments by approvers.

**Relationships:**
- Claims > User (1 to many)  
  - A lecturer can submit many claims, but each claim belongs to exactly one lecturer.
- Claims > Attachment (1 to many)  
  - A claim can have multiple documents.
- Claims > Approval (1 to 1)  
  - A claim has one approval.
- Approval > Status (1 to 1)  
  - Each approval has one status for a claim.

(Satzinger, Jackson and Burd, 2016)
---

### 1.3 GUI Layout

**Common features:**
- All Pages will have a navigation bar that will help the user navigate between the claim page, approver page and home page.
- Responsive layout for mobile and desktop compatibility.

**Claim Page:**  
Used by Lecturers to submit claims and track progress of claims.
- **Claims Table:**
  - Displays a table of all claims with full claim information.
  - Status (Pending, Approved, Not Approved) will have a link that will take the user to a details page that will contain all the claim's information and a comment left by the approver.
  - Document will have a link that allows the user to download the document of the claim.
- **Submit Claim:**
  - Date range selection.
  - Input for total hours worked.
  - Automatic calculation of total amount based on the user’s hourly rate.
  - Upload section for supporting documents.
  - Submit button to send claim.

**Approve Page:**  
Used by Coordinator / Academic Manager to review and approve or reject claims.
- **Claims Table:**
  - Displays all claims in a table.
  - Search/filter by lecturer name or date.
  - Action button that will take the user to an approve page.
- **Approve Claim Page:**
  - Displays claim details and uploaded documents.
  - A drop-down menu to approve or reject claim.
  - Text area to add comments (mandatory when rejecting).
  - A submit button that will update the claim’s status.

(Satzinger, Jackson and Burd, 2016)
---

### 1.4 Assumptions
- Each lecturer has a fixed hourly rate stored in the system.
- Claims can only be submitted for one month at a time.
- Uploaded documents must be PDF or image files (JPG/PNG).
- A claim cannot be edited once it is approved or rejected.
- Coordinators and Managers have equal authority to approve/reject claims.
- A login page/system may be needed.
- A query page may be needed.

(Satzinger, Jackson and Burd, 2016)
---

### 1.5 Constraints
- **Time constraint:** Claims must be submitted by a specific date.
- **File size limit:** Each uploaded document cannot exceed 5 MB.
- **Security constraint:** Files should be encrypted.
- **Concurrency:** Only one approver can work on a specific claim at a time to prevent conflicts.

(Satzinger, Jackson and Burd, 2016)
---

## QUESTION 2 : UML Class Diagram for Databases

<img width="682" height="354" alt="PROG 2B UMLNew drawio" src="https://github.com/user-attachments/assets/b452221d-fb8c-44d3-816c-0c458f49f6ce" />
Figure 1: UML Class Diagram (Draw.io) (Satzinger, Jackson and Burd, 2016)
---

## QUESTION 3 : Project Plan

![PLAN PROG](https://github.com/user-attachments/assets/0a7e9832-3b68-4e2c-8369-a4fca4d80bea)
Figure 2: Project Plan (Gido, Clements, Baker, Harinarain, Eresia-Eke, 2022)
---

## QUESTION 4

<img width="2981" height="2192" alt="Group 2 (3)" src="https://github.com/user-attachments/assets/c6e7d886-e4ed-429e-836b-62dd055d77c9" />
Figure 3: GUI for Contract Monthly Claim System (Figma, 2025)

---
##	REFERENCE LIST
-	Advancednet, 2025. APSess Login. [online] Available at: https://apsess.advnet.co.za/default.aspx [Accessed 16 September 2025].
-	Draw.io, 2025. Draw.io. [online] Available at: < https://app.diagrams.net > [Accessed 13 September 2025].
-	Figma, 2025. Figma. [online] Available at: < https://www.figma.com/ > [Accessed 13 September 2025].
- Gido, J., Clements, J., Baker, R., Harinarain, N., Eresia-Eke, C., 2022. Successful Project Management in South Africa. 2nd ed. United Kingdom: Cengage Learning.
- Satzinger, J.W., Jackson, R.B. and Burd, S.D., 2016. Systems Analysis and Design in a Changing World. 7th ed. Boston: Cengage Learning.
-	Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress. 
---
---
# Caleb Peter-John Els
# PROG 2B POE PART 2
# Contract Monthly Claim System (CMCS)
---
## YouTube Links
MVC: https://youtu.be/cUim7hp1BHA  
Unit Testing: https://youtu.be/llx5UVdeYxk  

---
## SQL Database
<img width="817" height="658" alt="image" src="https://github.com/user-attachments/assets/1cffa065-d472-4418-b6a5-5f82232a3c32" />
(Microsoft Learn, 2025a; Microsoft Learn, 2025b; The IIE. 2025)

---
## Lecturer Feedback
## UML Class Diagram for Databases: Accuracy and Completeness  
Feedback: PK and FK should be in curly brackets.   
Fix:  
<img width="940" height="455" alt="image" src="https://github.com/user-attachments/assets/e47662bd-7f43-4904-9905-2de5d36cf32b" />  
Figure 1: Updated UML Class Diagram (Draw.io) (Satzinger, Jackson and Burd, 2016)

## Project Plan: Realism and Achievability
Feedback: More detailed activities needed.     
Fix: 
<img width="1109" height="371" alt="image" src="https://github.com/user-attachments/assets/80fcee82-055c-4840-825e-11fbb0a82a98" />  
Figure 2: Updated Project Plan (Gido, Clements, Baker, Harinarain, Eresia-Eke, 2022)

## GUI UI: Design and User-Friendliness
Feedback: 
-Design needs to be improved, and colours need to change.  
-Explanations for each screen? Don't know which screen is which and for who?  
-Should be showing dummy data.   
-Should have input for additional notes with the claim. Missing requirements.  
Fix:  


<img width="940" height="469" alt="image" src="https://github.com/user-attachments/assets/4576c74d-4f36-40f0-b863-b326b0d3f2f1" />  

Figure 3: A welcome page, displaying navigation bar that has buttons to the other pages (this will also display on all the other pages)  


<img width="1365" height="627" alt="image" src="https://github.com/user-attachments/assets/81fa5902-0154-4154-8ac7-bb58aa7de1bb" />


Figure 4: Claims Portal where lecturers will view all the claims that they have submitted with an option to add new claim and to view more information about the status of the claim. They can also view the document they uploaded with the claim.  


<img width="1361" height="1206" alt="image" src="https://github.com/user-attachments/assets/161d5270-a5be-4c2b-9046-06b7c10be13b" />


Figure 5: This is the add claim view. The user will input the required information for their claim here.   
There is an input for users to add a document, their name, the start date, the end date, total hours and a comment.   
Total Amount will be added later on in Part 3.   
If the user tries to submit a claim that does not have a document or the correct type of document, it will display an error message.  


<img width="1365" height="627" alt="image" src="https://github.com/user-attachments/assets/f059966f-df6a-463d-9517-b5f13d390117" />


Figure 6: When the user click the Status(more details)  button, it will take the user to the Claim Status Page   
where it will give the user more details about the claim and it status.   


<img width="1365" height="625" alt="image" src="https://github.com/user-attachments/assets/60b92a64-2174-44e4-9ea8-40bb515d7fda" />

Figure 7: This is the Coordinators Portal. This will display the all the claims submitted, aswell as allow the user to view documents and verify the claim.  


<img width="1354" height="636" alt="image" src="https://github.com/user-attachments/assets/b5580672-f67d-4590-ad8a-b9033a03a0a0" />


Figure 8: The Verify Button on the Portal will take the user to a Verification Page where were the Coordinator will be  
giving a drop down to either Approve or Reject the verification of the claim.  

<img width="1365" height="638" alt="image" src="https://github.com/user-attachments/assets/966f5ecf-32de-4106-968e-8b8b92831861" />
 
 

Figure 9: This is the Manager Portal. This will display the all the claims submitted, as well as allow the user to view documents and approve the claim.  

<img width="1365" height="611" alt="image" src="https://github.com/user-attachments/assets/2ae7074e-a0ff-4f57-b836-7b038bd3e788" />

  

Figure 10: The Approve Button on the Portal will take the user to an Approval Page where were the Manager will be   
giving a drop down to either Approve or Reject the claim.  

---
##	REFERENCE LIST
-	Advancednet, 2025. APSess Login. [online] Available at: https://apsess.advnet.co.za/default.aspx [Accessed 16 September 2025].
-	Draw.io, 2025. Draw.io. [online] Available at: < https://app.diagrams.net > [Accessed 13 September 2025].
-	Figma, 2025. Figma. [online] Available at: < https://www.figma.com/ > [Accessed 13 September 2025].
- Gido, J., Clements, J., Baker, R., Harinarain, N., Eresia-Eke, C., 2022. Successful Project Management in South Africa. 2nd ed. United Kingdom: Cengage Learning.
- Microsoft Learn, 2025a. Reserved keywords (Transact-SQL). [online] Available at: https://learn.microsoft.com/en-us/sql/t-sql/language-elements/reserved-keywords-transact-sql?view=sql-server-ver17 [Accessed 20 October 2025].
- Microsoft Learn, 2025b. T-SQL naming issues - SQL code analysis. [online] Available at: https://learn.microsoft.com/en-us/sql/tools/sql-database-projects/concepts/sql-code-analysis/t-sql-naming-issues?view=sql-server-ver17 [Accessed 20 October 2025].
- Satzinger, J.W., Jackson, R.B. and Burd, S.D., 2016. Systems Analysis and Design in a Changing World. 7th ed. Boston: Cengage Learning.
- The IIE. 2025. Information System 2C [INSY7213 Module Manual]. The Independent Institute of Education: Unpublished.
-	Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress. 
---
---
# Caleb Peter-John Els
# PROG 2B POE PART 3
# Contract Monthly Claim System (CMCS)
---
## Users for accessing pages
- HR:  
  Username: Johnny  
  Passsword: 2003
- Lecturer:  
  Username: CalebL  
  Passsword: 2003
- Coordinator:  
  Username: GigiC  
  Passsword: 2003
- Manager:  
  Username: JolyneM  
  Passsword: 2003
  
---
## YouTube Links
Link: https://youtu.be/DM_uZV9xk24

---
## Lecturer Feedback
## Lecturers' Claim Submission: Implementation of Feature
Feedback: Design buttons should be the same size.     
Fix: All the buttons are the same colour and size.  
<img width="1355" height="690" alt="image" src="https://github.com/user-attachments/assets/a48520db-dab4-4d5d-b61a-ad8dbe05e723" />
Figure 1: Updated Claim Submission buttons. (Troelsen, A. and Japikse, P., 2022)

## Lecturers' Document Upload: Error Handling and Display
Feedback: Error handling should not remove all inputs.  
Fix: Error handing when creating a claim no longer removes all inputs.  
<img width="1344" height="683" alt="image" src="https://github.com/user-attachments/assets/85e5d276-6465-4a6a-bf25-ba1e9b693157" />
Figure 2: Updated Error handing for Claim Submission. (Troelsen, A. and Japikse, P., 2022)


---
## PowerPoint Presentation
[PROG 6212, PowerPoint Presentation, ST10306162.pptx](https://github.com/user-attachments/files/23656557/PROG.6212.PowerPoint.Presentation.ST10306162.pptx)

---
## Reference List
- The IIE. 2025. Information System 2C [INSY7213 Module Manual]. The Independent Institute of Education: Unpublished.
- Israel Quiroz, 2025. How to EASILY Create a PDF in C# .NET with QuestPDF! [video online] Available at: <https://www.youtube.com/watch?v=3qEPv-67iRg> [Accessed 20 November 2025].
- Nick Chapsas, 2023. The Easiest Way to Create PDFs in .NET. [video online] Available at: <https://www.youtube.com/watch?v=_M0IgtGWnvE&t=151s> [Accessed 20 November 2025].
- Programming with Felipe Gavilan, 2023. Generating PDFs with C# - Very Easy (two examples). [video online] Available at: <https://www.youtube.com/watch?v=bhR4Cmg16gs> [Accessed 20 November 2025].
- Microsoft, 2025a. DateTime.Compare(DateTime, DateTime) Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.datetime.compare?view=net-10.0>  [Accessed 19 November 2025].
- Microsoft, 2025b. DateTime.AddDays(Double) Method. [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/system.datetime.adddays?view=net-10.0>  [Accessed 19 November 2025].
- Microsoft, 2025c. IDocument Interface (org.w3c.dom). [online] Available at: <https://learn.microsoft.com/en-us/dotnet/api/org.w3c.dom.idocument?view=net-android-35.0> [Accessed 20 November 2025].
- QuestPDF, 2025a. API Reference. [online]Available at: <https://www.questpdf.com/api-reference> [Accessed 20 November 2025].
- QuestPDF, 2025b. Document Operations. [online] Available at: <https://www.questpdf.com/concepts/document-operations.html> [Accessed 20 November 2025].
- QuestPDF, 2025c. Generating Output. [online] Available at: <https://www.questpdf.com/concepts/generating-output.html> [Accessed 20 November 2025].
- QuestPDF, 2025d. License configuration. [online] Available at: <https://www.questpdf.com/license/configuration.html>  [Accessed 20 November 2025].
- Troelsen, A. and Japikse, P., 2022. Pro C# 10 with .NET 6: Foundational principles and practices in programming 8th ed. New York: Apress.




