import MockAdapter from 'axios-mock-adapter';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import apiClient from './client';
import { taskAPI } from './task';
import { todoAPI } from './todo';
import { workspaceAPI } from './workspace';

/**
 * 목록 API 가 ResponseDto 를 벗겨 페이지 객체를 그대로 돌려주는지 본다.
 * 응답을 배열로 다루면 조용히 빈 값이 되고 에러가 나지 않는다.
 */

const PAGE = {
  content: [{ id: 1, title: '할 일' }],
  totalPages: 3,
  totalElements: 41,
  number: 0,
  size: 20,
};

let mock;

beforeEach(() => {
  mock = new MockAdapter(apiClient);
});

afterEach(() => {
  mock.restore();
});

describe('목록 응답 형태', () => {
  it('Todo 목록은 페이지 객체를 그대로 돌려준다', async () => {
    mock.onGet('/todo').reply(200, { code: '200', data: PAGE });

    const result = await todoAPI.getByTask(1);

    expect(Array.isArray(result)).toBe(false);
    expect(result.content).toHaveLength(1);
    expect(result.totalPages).toBe(3);
  });

  it('Task 목록도 페이지 객체를 그대로 돌려준다', async () => {
    mock.onGet('/tasks/workspaces/1/tasks').reply(200, { code: '200', data: PAGE });

    const result = await taskAPI.getByWorkspace(1);

    expect(result.content).toHaveLength(1);
    expect(result.totalElements).toBe(41);
  });

  it('워크스페이스 진행률은 집계 객체를 돌려준다', async () => {
    mock.onGet('/tasks/workspaces/1/progress').reply(200, {
      code: '200',
      data: { total: 10, inProgress: 4, waitingReview: 3, confirmed: 3 },
    });

    const result = await taskAPI.getWorkspaceProgress(1);

    expect(result.total).toBe(10);
    expect(result.confirmed).toBe(3);
  });

  it('참여 워크스페이스 목록도 다른 목록과 같은 깊이로 벗긴다', async () => {
    mock.onGet('/workspaces/joined').reply(200, { code: '200', data: PAGE });

    const result = await workspaceAPI.getJoinedWorkspaces({});

    expect(result.content).toHaveLength(1);
    expect(result.totalPages).toBe(3);
  });

  it('워크스페이스 단건도 ResponseDto 를 한 겹 벗긴다', async () => {
    mock.onGet('/workspaces/1').reply(200, {
      code: '200',
      data: { id: 1, workspaceName: '팀' },
    });

    const result = await workspaceAPI.getById(1);

    expect(result.workspaceName).toBe('팀');
  });
});

describe('목록 파라미터', () => {
  it('Todo 목록은 taskId 와 페이지를 함께 보낸다', async () => {
    mock.onGet('/todo').reply(200, { data: PAGE });

    await todoAPI.getByTask(7, 'IN_PROGRESS', 2, 20, 'dueDate,asc');

    expect(mock.history.get[0].params).toMatchObject({
      taskId: 7,
      status: 'IN_PROGRESS',
      page: 2,
      size: 20,
      sort: 'dueDate,asc',
    });
  });

  it('정렬을 주지 않으면 sort 를 빼고 보낸다', async () => {
    mock.onGet('/todo').reply(200, { data: PAGE });

    await todoAPI.getByTask(7);

    expect(mock.history.get[0].params.sort).toBeUndefined();
  });

  it('Task 검색어가 비면 keyword 를 빼고 보낸다', async () => {
    mock.onGet('/tasks/workspaces/1/tasks').reply(200, { data: PAGE });

    await taskAPI.getByWorkspace(1, { keyword: '' });

    expect(mock.history.get[0].params.keyword).toBeUndefined();
  });

  it('Task 검색어가 있으면 그대로 보낸다', async () => {
    mock.onGet('/tasks/workspaces/1/tasks').reply(200, { data: PAGE });

    await taskAPI.getByWorkspace(1, { keyword: '검토', page: 1 });

    expect(mock.history.get[0].params).toMatchObject({ keyword: '검토', page: 1 });
  });

  it('기본 페이지와 크기가 들어간다', async () => {
    mock.onGet('/tasks/workspaces/1/tasks').reply(200, { data: PAGE });

    await taskAPI.getByWorkspace(1);

    expect(mock.history.get[0].params).toMatchObject({ page: 0, size: 20 });
  });
});
