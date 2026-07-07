use GLMS;

-- Create Clients Table
CREATE TABLE Clients (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(100) NOT NULL,
    ContactDetails NVARCHAR(255),
    Region NVARCHAR(100)
);

-- Create Contracts Table
CREATE TABLE Contracts (
    Id INT PRIMARY KEY IDENTITY(1,1),
    ClientId INT NOT NULL,
    StartDate DATETIME NOT NULL,
    EndDate DATETIME NOT NULL,
    Status NVARCHAR(50) DEFAULT 'Draft',
    ServiceLevel NVARCHAR(50),

    CONSTRAINT FK_Contracts_Clients FOREIGN KEY (ClientId)
        REFERENCES Clients(Id) ON DELETE CASCADE
);

-- Create ServiceRequests Table
CREATE TABLE ServiceRequests (
    Id INT PRIMARY KEY IDENTITY(1,1),
    ContractId INT NOT NULL,
    Description NVARCHAR(MAX),
    Cost DECIMAL(18,2) NOT NULL,
    Status NVARCHAR(50),

    CONSTRAINT FK_ServiceRequests_Contracts FOREIGN KEY (ContractId)
        REFERENCES Contracts(Id) ON DELETE CASCADE
);
-- Insert Clients
INSERT INTO Clients (Name, ContactDetails, Region)
VALUES 
('John Smith', 'john.smith@email.com', 'Gauteng'),
('Sarah Connor', '555-0199', 'Western Cape'),
('Thabo Mokoena', 'thabo.m@provider.co.za', 'KwaZulu-Natal');

-- Insert Contracts
INSERT INTO Contracts (ClientId, StartDate, EndDate, Status, ServiceLevel)
VALUES
(1, '2025-01-01', '2026-01-01', 'Active', 'Gold'),
(1, '2024-06-01', '2025-06-01', 'Draft', 'Silver'),
(2, '2025-03-15', '2026-03-15', 'Active', 'Platinum'),
(3, '2025-01-01', '2025-12-31', 'Active', 'Bronze');

-- Insert Service Requests
INSERT INTO ServiceRequests (ContractId, Description, Cost, Status)
VALUES
(1, 'System Installation', 1200.00, 'Completed'),
(1, 'Routine Maintenance', 450.50, 'Pending'),
(3, 'Hardware Replacement', 3500.00, 'Confirmed'),
(4, 'Software Update', 850.00, 'Cancelled');

-- (The IIE, 2025)

--REFERENCES
-- •	The IIE. 2025. Information System 2C [INSY7213 Module Manual]. The Independent Institute of Education: Unpublished.