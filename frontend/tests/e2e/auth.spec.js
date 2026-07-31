import { test, expect } from '@playwright/test';
let homepage = 'http://localhost:3000/';

test.describe('Auth End-to-End testing suite',()=>{

    test('Sign in as an existing user from landing page', async ({page})=>{
        const existingUser = {
            username: 'IAmR3al',
            password: 'J0hnDo3_'
        }
        const targetPage = homepage ;

        //Go to Landing Page
        await page.goto(targetPage);

        //Click signIn
        let signInButton = page.getByRole('button',{name: "Sign In"});
        await signInButton.click();

        //assert that we landed on the sign up page (if passes == continue)
        await expect(page).toHaveURL(/\/auth\/signin\/?$/);

        const usernameInput = page.getByPlaceholder('Username');
        const passwordInput = page.getByPlaceholder('Password');

        await usernameInput.fill(existingUser.username);
        await passwordInput.fill(existingUser.password);

        //click signuo again
        signInButton = page.getByRole('button',{name: "Sign In"});
        await signInButton.click();

        // verify
        await expect(page).toHaveURL(/.*\/library/);
    });

    test('Fail on non-existing user trying to sign in', async ({page})=>{
        const consoleErrors = [];


         const nonExistingUser = {
            username: 'IAmNotR3al',
            password: 'J0hnDo3_WillFail'
        }
        const targetPage = homepage ;

        //Go to Landing Page
        await page.goto(targetPage);

        //Click signUp
        let signInButton = page.getByRole('button',{name: "Sign In"});
        await signInButton.click();

        //assert that we landed on the sign up page (if passes == continue)
        await expect(page).toHaveURL(/\/auth\/signin\/?$/);

        const usernameInput = page.getByPlaceholder('Username');
        const passwordInput = page.getByPlaceholder('Password');

        await usernameInput.fill(nonExistingUser.username);
        await passwordInput.fill(nonExistingUser.password);

        //click signup again
        signInButton = page.getByRole('button',{name: "Sign In"});
        await signInButton.click();

        // verify
        await expect(page).toHaveURL(/\/auth\/signin\/?$/);
    });

    test('Sign up as a new user from landing page',async ({page})=>{
        const timestamp = Date.now();
        const newUserDetails = {
        firstName: 'Test',
        lastName: 'User',
        username: `user_${timestamp}`,
        email: `user_${timestamp}@gmail.com`,
        password: 'this15@5tr0nGpA$$Word'
        };

        const targetPage = homepage ;

        //Go to Landing Page
        await page.goto(targetPage);

        //Click signUp
        let signUpButton = page.getByRole('button',{name: "Sign Up"});
        await signUpButton.click();

        //check if it directed you properly
        await expect(page).toHaveURL(/\/auth\/signup\/?$/);

        const firstNameInput = page.getByPlaceholder('First Name');
        const lastNameInput = page.getByPlaceholder('Last Name');
        const usernameInput = page.getByPlaceholder('Username');
        const emailInput = page.getByPlaceholder('Email');
        const passwordInput = page.getByPlaceholder('Password').first();
        const confirmInput = page.getByPlaceholder('Confirm Password').last();


        //Enter User details
        await firstNameInput.fill(newUserDetails.firstName);
        await lastNameInput.fill(newUserDetails.lastName);
        await usernameInput.fill(newUserDetails.username);
        await emailInput.fill(newUserDetails.email);
        await passwordInput.fill(newUserDetails.password);
        await confirmInput.fill(newUserDetails.password);



        const signInButton = page.getByRole('button',{name: "Sign Up"});

        //click 
        await signInButton.click();

        // verify
        await expect(page).toHaveURL(/.*\/library/);

    });

    test('Fail on sign up',async ({page})=>{
        const brokenUserDetails = {
            firstName: 'Username',
            lastName: 'Willneverbeused',
            username: 'usernameWillNeverBeUsed',
            email: 'usernamewillneverbeused@gmail.com',
            password: 'weak' // this is a strong password
        };

        const targetPage = homepage ;

        //Go to Landing Page
        await page.goto(targetPage);

        //Click signUp
        let signUpButton = page.getByRole('button',{name: "Sign Up"});
        await signUpButton.click();

        //check if it directed you properly
        await expect(page).toHaveURL(/\/auth\/signup\/?$/);

        //fill in user details
        const firstNameInput = page.getByPlaceholder('First Name');
        const lastNameInput = page.getByPlaceholder('Last Name');
        const usernameInput = page.getByPlaceholder('Username');
        const emailInput = page.getByPlaceholder('Email');
        const passwordInput = page.getByPlaceholder('Password').first();
        const confirmInput = page.getByPlaceholder('Confirm Password').last();


        await firstNameInput.fill(brokenUserDetails.firstName);
        await lastNameInput.fill(brokenUserDetails.lastName);
        await usernameInput.fill(brokenUserDetails.username);
        await emailInput.fill(brokenUserDetails.email);
        await passwordInput.fill(brokenUserDetails.password);
        await confirmInput.fill(brokenUserDetails.password);


        const signInButton = page.getByRole('button',{name: "Sign Up"});

        //click 
        await signInButton.click();

        //Verify
        await expect(page).toHaveURL(/\/auth\/signup\/?$/);
        
        // alert needs to show up
        const alert = page.getByRole('alert').filter({ hasText: 'Missing or invalid required fields' });

        await expect(alert).toBeVisible();
    });

});