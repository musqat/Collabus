import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const BASE_URL = '/api';

let apiClient;
let clientMock;
let axiosMock;

// 인터셉터가 모듈 수준 상태(isRefreshing, pendingRequests)를 쓰므로
// 테스트마다 모듈을 다시 읽어 상태를 비운다
async function loadClient() {
  vi.resetModules();
  vi.stubEnv('VITE_API_BASE_URL', BASE_URL);
  const module = await import('./client');
  return module.default;
}

beforeEach(async () => {
  localStorage.clear();
  Object.defineProperty(window, 'location', {
    value: { href: '' },
    writable: true,
    configurable: true,
  });

  apiClient = await loadClient();
  clientMock = new MockAdapter(apiClient);
  // 재발급은 인스턴스가 아니라 axios 를 직접 쓴다
  axiosMock = new MockAdapter(axios);
});

afterEach(() => {
  clientMock.restore();
  axiosMock.restore();
  vi.unstubAllEnvs();
});

describe('요청 인터셉터', () => {
  it('저장된 토큰을 Authorization 에 붙인다', async () => {
    localStorage.setItem('accessToken', 'access-1');
    clientMock.onGet('/workspaces/joined').reply(200, { data: [] });

    await apiClient.get('/workspaces/joined');

    expect(clientMock.history.get[0].headers.Authorization).toBe('Bearer access-1');
  });

  it('토큰이 없으면 헤더를 붙이지 않는다', async () => {
    clientMock.onGet('/workspaces/joined').reply(200, { data: [] });

    await apiClient.get('/workspaces/joined');

    expect(clientMock.history.get[0].headers.Authorization).toBeUndefined();
  });
});

describe('401 재발급', () => {
  beforeEach(() => {
    localStorage.setItem('accessToken', 'expired');
    localStorage.setItem('refreshToken', 'refresh-1');
  });

  it('401 을 받으면 재발급하고 새 토큰으로 다시 부른다', async () => {
    axiosMock.onPost(`${BASE_URL}/token/refresh`).reply(200, {
      data: { accessToken: 'access-2', refreshToken: 'refresh-2' },
    });
    clientMock
      .onGet('/workspaces/joined')
      .replyOnce(401)
      .onGet('/workspaces/joined')
      .reply(200, { data: ['ok'] });

    const { data } = await apiClient.get('/workspaces/joined');

    expect(data.data).toEqual(['ok']);
    expect(localStorage.getItem('accessToken')).toBe('access-2');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-2');
    expect(clientMock.history.get[1].headers.Authorization).toBe('Bearer access-2');
  });

  it('동시에 401 을 받아도 재발급은 한 번만 한다', async () => {
    axiosMock.onPost(`${BASE_URL}/token/refresh`).reply(200, {
      data: { accessToken: 'access-2', refreshToken: 'refresh-2' },
    });
    clientMock.onGet('/tasks').replyOnce(401).onGet('/tasks').reply(200, { data: [] });
    clientMock.onGet('/todo').replyOnce(401).onGet('/todo').reply(200, { data: [] });

    await Promise.all([apiClient.get('/tasks'), apiClient.get('/todo')]);

    expect(axiosMock.history.post).toHaveLength(1);
  });

  it('기다리던 요청도 새 토큰으로 다시 나간다', async () => {
    axiosMock.onPost(`${BASE_URL}/token/refresh`).reply(200, {
      data: { accessToken: 'access-2', refreshToken: 'refresh-2' },
    });
    clientMock.onGet('/tasks').replyOnce(401).onGet('/tasks').reply(200, { data: [] });
    clientMock.onGet('/todo').replyOnce(401).onGet('/todo').reply(200, { data: [] });

    await Promise.all([apiClient.get('/tasks'), apiClient.get('/todo')]);

    const retried = clientMock.history.get.slice(2);
    expect(retried).toHaveLength(2);
    retried.forEach((request) => {
      expect(request.headers.Authorization).toBe('Bearer access-2');
    });
  });

  it('한 번 재시도한 요청은 또 재시도하지 않는다', async () => {
    axiosMock.onPost(`${BASE_URL}/token/refresh`).reply(200, {
      data: { accessToken: 'access-2', refreshToken: 'refresh-2' },
    });
    clientMock.onGet('/workspaces/joined').reply(401);

    await expect(apiClient.get('/workspaces/joined')).rejects.toThrow();

    expect(clientMock.history.get).toHaveLength(2);
    expect(axiosMock.history.post).toHaveLength(1);
  });
});

describe('재발급하지 않는 경우', () => {
  it('401 이 아니면 그대로 실패시킨다', async () => {
    localStorage.setItem('refreshToken', 'refresh-1');
    clientMock.onGet('/tasks/1').reply(403, { message: '권한 없음' });

    await expect(apiClient.get('/tasks/1')).rejects.toMatchObject({
      response: { status: 403 },
    });
    expect(axiosMock.history.post).toHaveLength(0);
  });

  it('로그인 401 은 재발급을 시도하지 않는다', async () => {
    localStorage.setItem('refreshToken', 'refresh-1');
    clientMock.onPost('/users/login').reply(401);

    await expect(apiClient.post('/users/login', {})).rejects.toThrow();

    expect(axiosMock.history.post).toHaveLength(0);
  });

  it('재발급 요청 자체의 401 은 다시 재발급하지 않는다', async () => {
    localStorage.setItem('refreshToken', 'refresh-1');
    clientMock.onPost('/token/refresh').reply(401);

    await expect(apiClient.post('/token/refresh', {})).rejects.toThrow();

    expect(axiosMock.history.post).toHaveLength(0);
  });

  it('refresh token 이 없으면 세션을 지우고 로그인으로 보낸다', async () => {
    localStorage.setItem('accessToken', 'expired');
    clientMock.onGet('/workspaces/joined').reply(401);

    await expect(apiClient.get('/workspaces/joined')).rejects.toThrow();

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(window.location.href).toBe('/login');
  });
});

describe('재발급 실패', () => {
  beforeEach(() => {
    localStorage.setItem('accessToken', 'expired');
    localStorage.setItem('refreshToken', 'refresh-1');
    localStorage.setItem('user', '{}');
  });

  it('세션을 모두 지우고 로그인으로 보낸다', async () => {
    axiosMock.onPost(`${BASE_URL}/token/refresh`).reply(401);
    clientMock.onGet('/workspaces/joined').reply(401);

    await expect(apiClient.get('/workspaces/joined')).rejects.toThrow();

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(window.location.href).toBe('/login');
  });

  it('기다리던 요청도 함께 실패시킨다', async () => {
    axiosMock.onPost(`${BASE_URL}/token/refresh`).reply(401);
    clientMock.onGet('/tasks').reply(401);
    clientMock.onGet('/todo').reply(401);

    const results = await Promise.allSettled([
      apiClient.get('/tasks'),
      apiClient.get('/todo'),
    ]);

    expect(results.every((r) => r.status === 'rejected')).toBe(true);
    expect(axiosMock.history.post).toHaveLength(1);
  });
});
