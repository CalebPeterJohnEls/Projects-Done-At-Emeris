USE master;
GO

-- Create the GLMSnew database only if it does not already exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'GLMSnew')
BEGIN
    CREATE DATABASE GLMSnew;
END
GO

-- Switch to the GLMSnew database
USE GLMSnew;
GO

-- Create Clients table
CREATE TABLE Clients (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(100) NOT NULL,
    ContactDetails NVARCHAR(255) NOT NULL,
    Region NVARCHAR(100) NOT NULL
);
GO

-- Create Contracts table
CREATE TABLE Contracts (
    Id INT PRIMARY KEY IDENTITY(1,1),
    ClientId INT NOT NULL,
    StartDate DATETIME NOT NULL,
    EndDate DATETIME NOT NULL,
    Status NVARCHAR(50) NOT NULL DEFAULT 'Draft',
    ServiceLevel NVARCHAR(50) NOT NULL,
    SignedAgreementPath NVARCHAR(MAX) NULL,
    FOREIGN KEY (ClientId) REFERENCES Clients(Id)
);
GO

-- Create ServiceRequests table
CREATE TABLE ServiceRequests (
    Id INT PRIMARY KEY IDENTITY(1,1),
    ContractId INT NOT NULL,
    Description NVARCHAR(MAX) NOT NULL,
    CostUSD DECIMAL(18,2) NOT NULL,
    CostZAR DECIMAL(18,2) NOT NULL,
    Status NVARCHAR(50) NOT NULL DEFAULT 'Draft',
    FOREIGN KEY (ContractId) REFERENCES Contracts(Id)
);
GO

-- Create Users table
CREATE TABLE Users (
    Username NVARCHAR(50) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Surname NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100) NOT NULL,
    Password NVARCHAR(100) NOT NULL,
    DateCreated DATETIME DEFAULT GETDATE()
);
GO

-- Insert data into Clients table
INSERT INTO Clients (Name, ContactDetails, Region)
VALUES
('John Smith', 'john.smith@email.com', 'Gauteng'),
('Sarah Connor', '555-0199', 'Western Cape'),
('Thabo Mokoena', 'thabo.m@provider.co.za', 'KwaZulu-Natal');
GO

-- Insert data into Contracts table
INSERT INTO Contracts (ClientId, StartDate, EndDate, Status, ServiceLevel, SignedAgreementPath)
VALUES
(1, '2025-01-01', '2026-01-01', 'Active', 'Gold', NULL),
(1, '2024-06-01', '2025-06-01', 'Draft', 'Silver', NULL),
(2, '2025-03-15', '2026-03-15', 'Active', 'Platinum', NULL),
(3, '2025-01-01', '2025-12-31', 'Active', 'Bronze', NULL);
GO

-- Insert data into ServiceRequests table
INSERT INTO ServiceRequests (ContractId, Description, CostUSD, CostZAR, Status)
VALUES
(1, 'System Installation', 1200.00, 21600.00, 'Completed'),
(1, 'Routine Maintenance', 450.50, 8109.00, 'Pending'),
(3, 'Hardware Replacement', 3500.00, 63000.00, 'Confirmed'),
(4, 'Software Update', 850.00, 15300.00, 'Cancelled');
GO

-- Insert data into Users table
INSERT INTO Users (Username, Name, Surname, Email, Password)
VALUES
('user1', 'John', 'Smith', 'john@email.com', '1234'),
('user2', 'Sarah', 'Connor', 'sarah@email.com', '1234'),
('user3', 'Thabo', 'Mokoena', 'thabo@email.com', '1234');
GO