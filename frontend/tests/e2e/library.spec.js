import { test, expect } from '@playwright/test';
import { assert } from 'node:console';
import { before, beforeEach } from 'node:test';
import { fi } from 'vuetify/locale';

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
  expect(CardTitle.trim()).toMatch(rulebookTitle.trim());

  //GO TO RULEBOOK PAGE
  const readRulebookButton = page
    .locator('nav.v-navigation-drawer--right')
    .getByRole('button', { name: 'Read Rulebook' });

  await readRulebookButton.click();

  await expect(page).toHaveURL(/\/library\/read\/.+/);
  

  //ensure the right rulebook is seen 
  const readingPageTitle = await page.locator('h1').textContent();
  expect(readingPageTitle.trim()).toMatch(CardTitle.trim());

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

});


