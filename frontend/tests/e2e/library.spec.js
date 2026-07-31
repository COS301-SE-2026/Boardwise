import { test, expect } from '@playwright/test';

let homepage = 'http://localhost:3000/'

//LAND ON LANDING PAGE AND GO TO LIBRARY
test.describe('Library End-to-End Testing',()=>{
// Navigate through rulebooks
test('Navigate From Landing page to Rulebooks and view a rulebook',async ({page})=>{
  const targetPage = homepage ;
  const sideBar = page.locator('nav.v-navigation-drawer--right');

  // land on page 
  await page.goto(targetPage);

  // click first rulebook button
  const rulebooksBtn = page.getByRole('button', { name: 'Rulebooks' }).first(); //top rulebook button 
  await rulebooksBtn.click();
  await expect(page).toHaveURL(/.*\/library/);
  await expect(page.getByRole('heading', { name: /library/i })).toBeVisible();

  //check to see if rulebook exists
  const rulebookCards =  page.locator('.d-flex.overflow-x-auto');

  //wait for cards to actually show up 
  await rulebookCards.waitFor({state: 'visible'});

  // find all the listing cards
  const cards = rulebookCards.locator('.v-card');

  //VERY FRAGILE BANDILEEEEE!!! 
  //wait to load
  await expect(cards.first()).toBeVisible({ timeout: 10000 }); // loading 

  //cards 
  const firstRulebookCard = cards.first();

  const CardTitle = await firstRulebookCard.locator('.text-body-2').first().textContent();

  await firstRulebookCard.click();


  //check if sidebar opened
  await expect(sideBar).toBeVisible({timeout: 5000});
  await expect(sideBar).toHaveClass(/v-navigation-drawer--active/);

  const rulebookTitle = await sideBar.locator('h2').textContent();  

  //check if loaded data matches for integrity purposes
  await expect(CardTitle.trim()).toMatch(rulebookTitle.trim());

  //GO TO RULEBOOK PAGE
  const readRulebookButton = page
    .locator('nav.v-navigation-drawer--right')
    .getByRole('button', { name: 'Read Rulebook' });

  await readRulebookButton.click();

  await expect(page).toHaveURL(/\/library\/read\/.+/);
  

  //ensure the right rulebook is seen 
  const readingPageTitle = await page.locator('h1').textContent();
  await expect(readingPageTitle.trim()).toMatch(CardTitle.trim());

}); 

test('Navigate from Landing page to Rulebooks And get redirected to signin', async ({page})=>{
  const targetPage = homepage ;
  const navBarRedirect = page.getByRole('link',{name: 'Marketplace'}).first();

  // land on page 
  await page.goto(targetPage);

  // click first rulebook button
  const rulebooksBtn = page.getByRole('button', { name: 'Rulebooks' }).first(); //top rulebook button 
  await rulebooksBtn.click();
  await expect(page).toHaveURL(/.*\/library/);
  await expect(page.getByRole('heading', { name: /library/i })).toBeVisible();

  //click
  await navBarRedirect.click();
  
  await expect(page).toHaveURL(/\/auth\/signin\/?$/);

});

//LOG IN And upload a rulebook, search for that rulebook, edit rulebook, view changes
test('Login to Boardwise, upload a rulebook and edit rulebook and view changes',async ({page})=>{
  //go to landing page 
  await page.goto(homepage+"auth/signin");
  await expect(page).toHaveURL(/.*\/auth\/signin/);

  //sign in
  const existingUser = {
    username: 'IAmR3al',
    password: 'J0hnDo3_'
  };

  const sideBar = page.locator('nav.v-navigation-drawer--right');

  const usernameInput = page.getByPlaceholder('Username');
  const passwordInput = page.getByPlaceholder('Password');

  await usernameInput.fill(existingUser.username);
  await passwordInput.fill(existingUser.password);

  //click again
  const signInButton = page.getByRole('button',{name: "Sign In"});
  await signInButton.click();

  //check redirect
  await expect(page).toHaveURL(/.*\/library/);

  //click upload
  const uploadRulebook = page.getByRole('button',{name: 'Upload Rulebook'});
  await uploadRulebook.click();
  await expect(page.getByRole('heading', { name: 'Upload a Rulebook' })).toBeVisible();

  //Upload Rulebook modal opens
  // await expect(page.getByTestId('title-autocomplete-loading')).toBeHidden();

  let  rulebookTitle = page.locator('.v-autocomplete input');
  const rulebookLanguage = page.getByPlaceholder('Language');

  //fill in with 
  const title =  'Life-Boat Ludo';
  await rulebookTitle.fill(title);

  await page.getByRole('option', {name:title}).first().click();
  await rulebookLanguage.fill('English');

  //click on upload
  const fileInput = page.locator('input[type="file"]');
  await fileInput.setInputFiles('tests/e2e/Ludo_rb_test.pdf');
  
  //check if name pops up (cached)
  await expect(page.getByText('Ludo_rb_test.pdf')).toBeVisible();
  await expect(page.getByText('Life-Boat Ludo').last()).toBeVisible();

  //find add button
  const addButton = page.getByRole('button',{name:'add'});
  await addButton.click();
  
  //refresh
  await page.waitForTimeout(5000);// 5 seconds 
  await page.reload();

  //check if the rulebook was uploaded
  const searchForNewRuleBook = page.getByPlaceholder('Search for rulebooks...');
  searchForNewRuleBook.fill(title);

  //check to see if rulebook exists
  const rulebookCards =  page.locator('.d-flex.overflow-x-auto');

  //wait for cards to actually show up 
  await rulebookCards.waitFor({state: 'visible'});

  // find all the listing cards
  const cards = rulebookCards.locator('.v-card');

  //check if the only card is there 
  //VERY FRAGILE BANDILEEEEE!!! 
  //wait to load
  await expect(cards.first()).toBeVisible({ timeout: 10000 }); // loading 

  //cards 
  const firstRulebookCard = cards.first();

  const CardTitle = await firstRulebookCard.locator('.text-body-2').first().textContent();

  expect(CardTitle).toMatch(title);

  await firstRulebookCard.click();


  //check if sidebar opened
  await expect(sideBar).toBeVisible({timeout: 5000});
  await expect(sideBar).toHaveClass(/v-navigation-drawer--active/);

  rulebookTitle = await sideBar.locator('h2').textContent();  

  //check if loaded data matches for integrity purposes
  expect(CardTitle.trim()).toMatch(rulebookTitle.trim());

  //GO TO RULEBOOK PAGE
  const readRulebookButton = page
    .locator('nav.v-navigation-drawer--right')
    .getByRole('button', { name: 'Read Rulebook' });

  await readRulebookButton.click();

  await expect(page).toHaveURL(/\/library\/read\/.+/);
  
  //ensure the right rulebook is seen 
  const readingPageTitle = await page.locator('h1').textContent();
  expect(readingPageTitle.trim()).toMatch(title.trim());

  //EDITING

  //acquire lock 
  await page.getByRole('button', {name: 'Edit'}).click();
  
  //write to the first section 
  const writingSection =page.getByPlaceholder('Edit section content...');
  const text = await writingSection.textContent();
  if(text.length > 0){
    // clear it out
    await writingSection.fill('');
  } 

  const details = 'This is a Test edit from : ' + `${Date.now()}`
  await writingSection.fill(details);

  //click save 
  await page.getByRole('button', {name: 'Save'}).click();
  await page.waitForTimeout(200);
  
  //check if section has been overwritten
  await expect(writingSection).toHaveValue(details);  

  //check history 
  await page.locator('.mdi-history').click();

  const historyPanel = page.locator('nav.v-navigation-drawer--right');
  await expect(historyPanel.getByRole('heading', { name: 'Edit History' })).toBeVisible();

  // assert at least one history entry exists
  const historyEntries = historyPanel.locator('.history-entry');
  await expect(historyEntries.first()).toBeVisible();
});


});


