import { test as base, expect, request } from '@playwright/test';

/**
 * 시드 계정. V2 마이그레이션이 넣는다.
 */
export const ACCOUNTS = {
  master: { email: 'user1@test.com', password: 'password', displayName: 'user1#0001' },
  member: { email: 'user2@test.com', password: 'password', displayName: 'user2#0002' },
};

const API = process.env.API_URL ?? 'http://localhost:8080';

/** 이번 테스트가 만든 자원. afterEach 에서 지운다 */
const created = { workspaceIds: [], accounts: [] };

export const test = base.extend({
  // 테스트가 끝나면 만든 자원을 지운다. 모든 테스트에 자동으로 붙는다
  autoCleanup: [async ({}, use) => {
    await use();
    await cleanup();
  }, { auto: true }],

  loggedIn: async ({ page }, use) => {
    await login(page, ACCOUNTS.master);
    await use(page);
  },
});

/**
 * 테스트가 만든 워크스페이스와 계정을 지운다.
 * 워크스페이스를 지우면 하위 Task·Todo·첨부까지 캐스케이드로 정리된다.
 */
export async function cleanup() {
  if (!created.workspaceIds.length) {
    created.accounts = [];
    return;
  }
  const api = await request.newContext({ baseURL: API });

  const auth = await api.post('/api/users/login', {
    data: { email: ACCOUNTS.master.email, password: ACCOUNTS.master.password },
  });
  const token = (await auth.json()).data.accessToken;
  const headers = { Authorization: `Bearer ${token}` };

  for (const id of created.workspaceIds) {
    await api.delete(`/api/workspaces/${id}`, { headers });
  }
  // 계정은 지우지 않는다. 회원 탈퇴가 FK 때문에 500 을 낸다
  created.workspaceIds = [];
  created.accounts = [];
  await api.dispose();
}

export async function login(page, account) {
  await page.goto('/login');
  await page.locator('input[type="email"]').fill(account.email);
  await page.locator('input[type="password"]').fill(account.password);
  await page.getByRole('button', { name: '로그인' }).click();
  await page.waitForURL('**/dashboard');
}

/**
 * 워크스페이스 → Task → Todo → 작업 내용 → 댓글을 API 로 만들고 id 를 돌려준다.
 * 화면 테스트가 세션에 남아 있는 데이터에 기대지 않게 한다.
 */
export async function seedWorkspace(account = ACCOUNTS.master) {
  const api = await request.newContext({ baseURL: API });

  const auth = await api.post('/api/users/login', {
    data: { email: account.email, password: account.password },
  });
  const token = (await auth.json()).data.accessToken;
  const headers = { Authorization: `Bearer ${token}` };
  const stamp = Date.now();

  const ws = await api.post('/api/workspaces', {
    headers,
    data: { workspaceName: `e2e-${stamp}`, description: 'e2e' },
  });
  const workspaceId = (await ws.json()).id;

  const task = await api.post('/api/tasks', {
    headers,
    data: {
      workspaceId,
      title: `e2e-task-${stamp}`,
      description: 'e2e task',
      dueDate: '2030-12-31',
    },
  });
  const taskId = (await task.json()).data.id;

  const todo = await api.post('/api/todo', {
    headers,
    data: { taskId, title: `e2e-todo-${stamp}`, description: 'e2e todo', dueDate: '2030-12-31' },
  });
  const todoId = (await todo.json()).data.id;

  await api.post(`/api/todo/works?todoId=${todoId}`, {
    headers,
    data: { title: 'e2e work', content: 'e2e work content' },
  });
  await api.post(`/api/todo/comments?todoId=${todoId}`, {
    headers,
    data: { content: 'e2e comment' },
  });

  await api.dispose();
  created.workspaceIds.push(workspaceId);
  return { workspaceId, taskId, todoId, name: `e2e-${stamp}` };
}

/**
 * 워크스페이스에 다른 계정을 멤버로 넣는다. 초대 후 수락까지 마친다.
 */
export async function addMember(workspaceId, invitee = ACCOUNTS.member) {
  const api = await request.newContext({ baseURL: API });

  const masterAuth = await api.post('/api/users/login', {
    data: { email: ACCOUNTS.master.email, password: ACCOUNTS.master.password },
  });
  const masterToken = (await masterAuth.json()).data.accessToken;

  const inviteeAuth = await api.post('/api/users/login', {
    data: { email: invitee.email, password: invitee.password },
  });
  const inviteeBody = (await inviteeAuth.json()).data;

  await api.post(`/api/workspaces/${workspaceId}/invites`, {
    headers: { Authorization: `Bearer ${masterToken}` },
    data: { userId: inviteeBody.id, role: 'MEMBER' },
  });

  const invites = await api.get('/api/workspaces/invites/me?size=100', {
    headers: { Authorization: `Bearer ${inviteeBody.accessToken}` },
  });
  const target = (await invites.json()).data.content
    .find((invite) => invite.workspaceId === workspaceId);

  await api.post(`/api/workspaces/invites/${target.inviteId}/accept`, {
    headers: { Authorization: `Bearer ${inviteeBody.accessToken}` },
  });

  await api.dispose();
  return target.inviteId;
}

/**
 * 초대만 보내고 수락하지 않는다.
 */
export async function invite(workspaceId, invitee = ACCOUNTS.member) {
  const api = await request.newContext({ baseURL: API });
  const auth = await api.post('/api/users/login', {
    data: { email: ACCOUNTS.master.email, password: ACCOUNTS.master.password },
  });
  const token = (await auth.json()).data.accessToken;

  const inviteeAuth = await api.post('/api/users/login', {
    data: { email: invitee.email, password: invitee.password },
  });
  const inviteeId = (await inviteeAuth.json()).data.id;

  await api.post(`/api/workspaces/${workspaceId}/invites`, {
    headers: { Authorization: `Bearer ${token}` },
    data: { userId: inviteeId, role: 'MEMBER' },
  });
  await api.dispose();
}

/**
 * 새 계정을 만들어 돌려준다. 초대처럼 상태가 쌓이는 흐름을 격리한다.
 */
export async function createUser() {
  const api = await request.newContext({ baseURL: API });
  const stamp = Date.now();
  const account = {
    email: `e2e-${stamp}@test.com`,
    password: 'Passw0rd!23',
    nickname: `e2e${stamp}`,
  };

  await api.post('/api/users/register', { data: account });
  await api.dispose();
  created.accounts.push(account);
  return account;
}

export { expect };
