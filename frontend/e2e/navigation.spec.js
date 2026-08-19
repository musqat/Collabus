import { test, expect, seedWorkspace } from './fixtures';

test.describe('기본 흐름', () => {
  test('로그인하면 대시보드로 간다', async ({ loggedIn: page }) => {
    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.getByRole('heading', { name: '워크스페이스', level: 3 })).toBeVisible();
  });

  test('워크스페이스를 열면 Task 목록이 보인다', async ({ loggedIn: page }) => {
    const { workspaceId } = await seedWorkspace();

    await page.goto(`/workspace/${workspaceId}`);
    await expect(page.getByRole('heading', { name: 'Task', exact: true })).toBeVisible();
    await expect(page.getByRole('heading', { name: /e2e-task-/ })).toBeVisible();
  });

  test('사이드바 트리를 펼치면 Task 가 나온다', async ({ loggedIn: page }) => {
    // 사이드바 워크스페이스 이름은 버튼으로 그려진다
    const workspaceButtons = page.getByRole('button');
    const before = await workspaceButtons.count();

    // 이름 버튼 왼쪽의 화살표가 펼치기 토글이다
    await page.getByRole('button').nth(2).click();

    await expect.poll(() => workspaceButtons.count(), { timeout: 10000 })
      .toBeGreaterThan(before);
  });
});
