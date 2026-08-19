import { test, expect, login, seedWorkspace, invite, createUser,
  joinCommentAndWithdraw, ACCOUNTS } from './fixtures';

test.describe('회원 탈퇴', () => {
  test('탈퇴하면 로그인할 수 없다', async ({ page }) => {
    const account = await createUser();

    await login(page, account);
    await page.goto('/profile');

    // 탈퇴는 이메일을 그대로 입력해야 진행된다
    await page.getByRole('button', { name: '계정 삭제', exact: true }).click();
    await page.getByPlaceholder('이메일 입력').fill(account.email);
    await page.getByRole('button', { name: '삭제', exact: true }).click();
    await page.waitForURL('**/login');

    await page.locator('input[type="email"]').fill(account.email);
    await page.locator('input[type="password"]').fill(account.password);
    await page.getByRole('button', { name: '로그인' }).click();

    await expect(page).toHaveURL(/\/login/);
  });

  test('탈퇴해도 남긴 댓글은 남고 작성자만 익명이 된다', async ({ page }) => {
    const seeded = await seedWorkspace();
    const { workspaceId, todoId } = seeded;
    const account = await createUser();
    await invite(workspaceId, account);
    await joinCommentAndWithdraw(seeded, account, 'comment before withdrawal');

    await login(page, ACCOUNTS.master);
    await page.goto(`/todo/${todoId}`);
    await page.getByRole('button', { name: /댓글/ }).click();

    await expect(page.getByText('comment before withdrawal')).toBeVisible();
    await expect(page.getByText('탈퇴한 사용자', { exact: false }).first()).toBeVisible();
  });
});
