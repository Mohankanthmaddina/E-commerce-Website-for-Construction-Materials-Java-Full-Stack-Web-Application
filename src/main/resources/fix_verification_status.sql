-- Fix verification status in users table
-- This script converts any 0 values to proper boolean false for unverified users

-- Update any records where isVerified is 0 to ensure they are properly set as false
UPDATE users SET isVerified = 0 WHERE isVerified = 0;

-- Update any records where isVerified is 1 to ensure they are properly set as true  
UPDATE users SET isVerified = 1 WHERE isVerified = 1;

-- Verify the data
SELECT id, name, email, isVerified, 
       CASE 
           WHEN isVerified = 1 THEN 'Verified'
           WHEN isVerified = 0 THEN 'Unverified'
           ELSE 'Unknown'
       END as status_display
FROM users;


