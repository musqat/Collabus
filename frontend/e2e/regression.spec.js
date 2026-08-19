import { test, expect, seedWorkspace } from './fixtures';

/**
 * 목록 응답을 잘못 다뤄 화면이 조용히 비던 버그들의 회귀 방지.
 */
test.describe('목록 응답 회귀', () => {
  test('Task 카드에 진행률이 보인다', async ({ loggedIn: page }) => {
    const { workspaceId } = await seedWorkspace();

    await page.goto(`/workspace/${workspaceId}`);
    await expect(page.getByRole('heading', { name: 'Task', exact: true })).toBeVisible();

    // 진행률 API 가 총계를 주지 못하면 카드에서 진행률 영역이 사라진다
    await expect(page.getByText('%').first()).toBeVisible();
  });

  test('작업 내용과 댓글 날짜가 Invalid Date 가 아니다', async ({ loggedIn: page }) => {
    const { todoId } = await seedWorkspace();

    await page.goto(`/todo/${todoId}`);
    await expect(page.getByRole('heading', { name: 'e2e work' })).toBeVisible();
    await expect(page.getByText('Invalid Date')).toHaveCount(0);

    await page.getByRole('button', { name: /댓글/ }).click();
    await expect(page.getByText('e2e comment')).toBeVisible();
    await expect(page.getByText('Invalid Date')).toHaveCount(0);
  });

  test('사이드바 트리를 펼치면 Todo 까지 나온다', async ({ loggedIn: page }) => {
    const { name } = await seedWorkspace();

    await page.reload();
    const workspaceButton = page.getByRole('button', { name }).first();
    await expect(workspaceButton).toBeVisible();

    // 이름 버튼 바로 앞의 화살표가 펼치기 토글이다
    const row = page.locator('div').filter({ has: workspaceButton }).last();
    await row.getByRole('button').first().click();

    await expect(page.getByRole('button', { name: /e2e-task-/ })).toBeVisible();
  });
});
