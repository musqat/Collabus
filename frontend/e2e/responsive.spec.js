import { test, expect, seedWorkspace } from './fixtures';

// 좁은 화면 최소 기준
test.use({ viewport: { width: 360, height: 780 } });

// 화면 밖으로 나간 요소. 본문 안에 따로 둔 가로 스크롤 상자 속 요소는 제외
async function overflowingElements(page) {
  return page.evaluate(() => {
    const vw = document.documentElement.clientWidth;
    const insideScroller = (el) => {
      for (let p = el.parentElement; p && p.tagName !== 'MAIN'; p = p.parentElement) {
        const x = getComputedStyle(p).overflowX;
        if (x === 'auto' || x === 'scroll') return true;
      }
      return false;
    };
    return [...document.querySelectorAll('body *')]
      .filter((el) => {
        const r = el.getBoundingClientRect();
        return r.width > 0 && r.height > 0 && (r.right > vw + 1 || r.left < -1);
      })
      .filter((el) => !insideScroller(el))
      .map((el) => `${el.tagName.toLowerCase()} "${(el.textContent || '').trim().slice(0, 20)}"`);
  });
}

test.describe('좁은 화면', () => {
  test('로그인 뒤 화면들이 360px 밖으로 넘치지 않는다', async ({ loggedIn: page }) => {
    const { workspaceId, taskId, todoId } = await seedWorkspace();

    for (const path of ['/dashboard', `/workspace/${workspaceId}`, `/task/${taskId}`, `/todo/${todoId}`, '/invitations', '/profile']) {
      await page.goto(path);
      await page.waitForLoadState('networkidle');
      expect(await overflowingElements(page), path).toEqual([]);
    }
  });

  test('사이드바는 메뉴 버튼으로 여는 서랍이고 화면을 옮기면 닫힌다', async ({ loggedIn: page }) => {
    const { name } = await seedWorkspace();
    await page.goto('/dashboard');

    await expect(page.getByRole('button', { name: '사이드바 접기' })).toBeHidden();

    await page.getByRole('button', { name: '메뉴 열기' }).click();
    await expect(page.getByRole('button', { name: '사이드바 접기' })).toBeVisible();

    const drawer = page.locator('div.fixed.inset-0.z-40');
    await drawer.getByRole('button', { name, exact: true }).click();

    await expect(page).toHaveURL(/\/workspace\/\d+/);
    await expect(page.getByRole('button', { name: '사이드바 접기' })).toBeHidden();
  });
});
