import { test, expect, seedWorkspace } from './fixtures';

test.describe('페이지네이션과 정렬', () => {
  test('페이지 버튼이 URL 을 바꾸고 새로고침해도 유지된다', async ({ loggedIn: page }) => {
    // 목록이 그려진 뒤에 페이지네이션이 붙는다
    await expect(page.locator('a[href^="/workspace/"]').first()).toBeVisible();

    const pagination = page.locator('nav[aria-label="페이지"]');
    if (!(await pagination.count())) {
      test.skip(true, '워크스페이스가 한 페이지에 다 들어간다');
    }
    const next = pagination.getByRole('button', { name: '2', exact: true });

    await next.click();
    await expect(page).toHaveURL(/[?&]page=2/);

    await page.reload();
    await expect(page).toHaveURL(/[?&]page=2/);
    await expect(pagination.getByRole('button', { name: '2', exact: true }))
      .toHaveAttribute('aria-current', 'page');
  });

  test('정렬을 바꾸면 URL 에 실리고 첫 페이지로 돌아간다', async ({ loggedIn: page }) => {
    const sort = page.getByLabel('정렬 기준');
    await sort.selectOption('workspaceName,asc');

    await expect(page).toHaveURL(/sort=workspaceName%2Casc/);
    await expect(page).not.toHaveURL(/[?&]page=/);
  });

  test('Task 검색이 서버로 나가 결과 수가 바뀐다', async ({ loggedIn: page }) => {
    const { workspaceId } = await seedWorkspace();
    await page.goto(`/workspace/${workspaceId}`);

    const search = page.getByPlaceholder('Task 검색...');
    await search.fill('없을만한검색어zzz');

    await expect(page.getByText('0개 결과')).toBeVisible();
  });
});
