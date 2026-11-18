  import "@testing-library/jest-dom/vitest";
  import { cleanup } from "@testing-library/react";
  import { afterEach } from "vitest";

  afterEach(() => {
    cleanup();
    localStorage.clear();
  });

/* Reference List
jsdom, 2026. jsdom: a JavaScript implementation of the WHATWG DOM and HTML standards. [online] Available at: https://github.com/jsdom/jsdom [Accessed 12 October 2026].
Testing Library, 2026 a. React Testing Library. [online] Available at: https://testing-library.com/docs/react-testing-library/intro/ [Accessed 12 October 2026].
Testing Library, 2026 b. user-event. [online] Available at: https://testing-library.com/docs/user-event/intro [Accessed 12 October 2026].
Testing Library, 2026 c. jest-dom. [online] Available at: https://github.com/testing-library/jest-dom [Accessed 12 October 2026].
Vitest, 2026 a. Getting started. [online] Available at: https://vitest.dev/guide/ [Accessed 12 October 2026].
Vitest, 2026 b. Configuring Vitest. [online] Available at: https://vitest.dev/config/ [Accessed 12 October 2026].
Vitest, 2026 c. Mocking. [online] Available at: https://vitest.dev/guide/mocking [Accessed 12 October 2026].*/