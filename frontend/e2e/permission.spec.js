import { test, expect, login, seedWorkspace, addMember, invite, createUser, ACCOUNTS } from './fixtures';

test.describe('권한', () => {
  test('MEMBER 는 참여하지 않은 Task 에 들어갈 수 없다', async ({ page }) => {
    const { workspaceId, taskId } = await seedWorkspace();
    // 워크스페이스에는 넣되 Task 참여자로는 넣지 않는다
    await addMember(workspaceId);

    await login(page, ACCOUNTS.member);
    await page.goto(`/task/${taskId}`);

    // 권한이 없으면 화면이 Todo 목록을 그리지 못한다
    await expect(page.getByRole('heading', { name: 'Todo', exact: true })).toHaveCount(0);
  });

  test('MEMBER 에게는 Task 추가 버튼이 없다', async ({ page }) => {
    const { workspaceId } = await seedWorkspace();
    await addMember(workspaceId);

    await login(page, ACCOUNTS.member);
    await page.goto(`/workspace/${workspaceId}`);

    await expect(page.getByRole('heading', { name: 'Task', exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Task 추가' })).toHaveCount(0);
  });
});

test.describe('초대', () => {
  test('초대를 수락하면 워크스페이스 목록에 나온다', async ({ page }) => {
    const { workspaceId, name } = await seedWorkspace();
    // 새 계정이라 받은 초대가 이것 하나뿐이다
    const invitee = await createUser();
    await invite(workspaceId, invitee);

    await login(page, invitee);
    await page.goto('/invitations');
    await expect(page.getByRole('heading', { name })).toBeVisible();

    await page.getByRole('button', { name: '수락' }).click();

    await page.goto('/dashboard');
    await expect(page.getByRole('heading', { name })).toBeVisible();
  });
});

test.describe('인증', () => {
  test('비밀번호가 틀리면 로그인되지 않는다', async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[type="email"]').fill(ACCOUNTS.master.email);
    await page.locator('input[type="password"]').fill('wrong-password');
    await page.getByRole('button', { name: '로그인' }).click();

    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByText(/일치하지|실패/)).toBeVisible();
  });

  test('로그인하지 않으면 대시보드에 접근할 수 없다', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/);
  });
});
