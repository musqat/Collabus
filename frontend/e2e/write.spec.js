import { test, expect, seedWorkspace } from './fixtures';

test.describe('생성과 상태 전이', () => {
  test('Task 를 만들면 목록과 사이드바에 함께 나온다', async ({ loggedIn: page }) => {
    const { workspaceId, name } = await seedWorkspace();
    const title = `new-task-${Date.now()}`;

    await page.goto(`/workspace/${workspaceId}`);
    await page.getByRole('button', { name: 'Task 추가' }).click();

    await page.getByPlaceholder('Task 제목을 입력하세요').fill(title);
    await page.locator('input[type="date"]').fill('2030-12-31');

    await page.getByRole('button', { name: '생성', exact: true }).click();

    // 목록에 바로 붙는다
    await expect(page.getByRole('heading', { name: title })).toBeVisible();

    // 사이드바 트리도 같은 무효화로 갱신된다
    const workspaceButton = page.getByRole('button', { name, exact: true });
    const row = page.locator('div').filter({ has: workspaceButton }).last();
    await row.getByRole('button').first().click();
    await expect(page.getByRole('button', { name: title })).toBeVisible();
  });

  test('완료 요청과 승인이 진행률에 반영된다', async ({ loggedIn: page }) => {
    const { taskId } = await seedWorkspace();

    await page.goto(`/task/${taskId}`);
    await expect(page.getByRole('heading', { name: 'Todo', exact: true })).toBeVisible();

    await page.getByRole('button', { name: '작업완료' }).click();
    await expect(page.getByRole('button', { name: '승인' })).toBeVisible();

    await page.getByRole('button', { name: '승인' }).click();

    // 진행률 집계가 완료로 바뀐다
    await expect(page.getByText('총 1개의 Todo')).toBeVisible();
    await expect(page.locator('td').filter({ hasText: '완료' }).first()).toBeVisible();
  });
});
