import { test, expect } from '@playwright/test';
import { assert } from 'node:console';
import { beforeEach } from 'node:test';

let homepage = 'http://localhost:3000/'
//LAND ON LANDING PAGE AND GO TO LIBRARY
test('Landing page with unauthorised access to library',async ({page})=>{
  //ARRANGE 
  const targetPage = homepage ;

  //ACT
  await page.goto(targetPage);
  await page.waitForLoadState('networkidle'); 

  const rulebooksBtn = page.getByRole('button', { name: 'Rulebooks' }).first(); //top rulebook button 
  await rulebooksBtn.click();

  //ASSERT
  await expect(page).toHaveURL(/.*\/library/);
  await expect(page.getByRole('heading', { name: /library/i })).toBeVisible();
}) //check if it took you to Library