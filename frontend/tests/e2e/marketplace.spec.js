import { test, expect } from '@playwright/test';

const homepage =  'http://localhost:3000/';

test.describe('Marketplace Service Tests',()=>{
    test('Logged in user creates a listing, , view listing details, update listing  removes that listing', async({page})=>{
        
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
        await expect(page).toHaveURL(homepage);


        const usernameInput = page.getByPlaceholder('Username');
        const passwordInput = page.getByPlaceholder('Password');

        await usernameInput.fill(existingUser.username);
        await passwordInput.fill(existingUser.password);

        //click signup again
        signInButton = page.getByRole('button',{name: "Sign In"});
        await signInButton.click();

        // verify
        await expect(page).toHaveURL(/.*\/library/);

        //navigate to marketplace
        const navBarRedirect = page.getByRole('link',{name: 'Marketplace'}).first();
        
        // land on page 
        await page.goto(targetPage);
        

        //click
        await navBarRedirect.click();
        //should be at marketplace
        await expect(page).toHaveURL(/\/marketplace\/?$/);

        //click on create Listing
        await page.getByRole('button', { name: 'Create Listing' }).click();

        const listingTitle = page.getByLabel('Listing Title');
        const gameTitle = page.getByLabel('Game Title').first();
        const gameVersion = page.getByPlaceholder('e.g. Original');

        const compListingTitle = 'RandomBoardGameToBeDeleted';
        await listingTitle.fill(compListingTitle);

        // const compGameTitle = 'Die Macher';
        const compGameTitle = 'Monopoly';
        await gameTitle.click();
        await gameTitle.fill(compGameTitle);
        await page.waitForTimeout(400);
        await page.getByRole('option', { name: compGameTitle }).first().click();

        const compVersion = 'Original';
        await gameVersion.fill(compVersion);

        // genres
        const genresInput = page.getByLabel('Genres').first();
        await genresInput.click();
        await genresInput.fill('eco');
        await page.waitForTimeout(400);
        await page.getByRole('option', { name: 'economic' }).first().click();

        await page.getByRole('heading', { name: 'Create Listing' }).click();
        await page.waitForTimeout(200);

        // condition
        await page.getByLabel('Condition').click({ force: true });
        await page.getByRole('option', { name: 'Like New' }).first().click();

        // item type
        await page.getByLabel('Item Type').click({ force: true });
        await page.getByRole('option', { name: 'Full Boardgame' }).first().click();

        // price
        const amount = page.getByLabel('Amount');
        await amount.fill('250');

        // location
        const compLo = 'Middleburg Mpumalanga';
        const location = page.getByPlaceholder('e.g. Pretoria');
        await location.fill(compLo);

        // description
        const compdesc = 'This is genuinely some text';
        const description = page.getByPlaceholder('description');
        await description.fill(compdesc);

        const fileInput = page.locator('input[type="file"]');
        await fileInput.setInputFiles('tests/e2e/resources/listingImage.jpg');

        await page.getByRole('button', { name: 'Create Listing' }).last().click();

        const listingSearch = page.getByPlaceholder('Search for listings...');
        await listingSearch.fill(compListingTitle);

        const card = page.locator('.v-card');
        await card.first().waitFor({ state: 'visible' });
        await card.first().click();

        await expect(page).toHaveURL(/\/marketplace\/.+/);

    })
});