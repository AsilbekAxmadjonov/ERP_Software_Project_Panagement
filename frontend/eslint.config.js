import js from '@eslint/js';
import tseslint from 'typescript-eslint';

const modules = ['identity', 'partners', 'accounting', 'inventory', 'sales', 'purchase', 'audit', 'expenses', 'employees'];

// A module's frontend may not import another module's code; only the shell composes modules.
const boundaryRules = modules.map((name) => ({
  files: [`src/modules/${name}/**/*.{ts,tsx}`],
  rules: {
    'no-restricted-imports': [
      'error',
      {
        patterns: [
          ...modules.filter((m) => m !== name).map((m) => ({ group: [`**/modules/${m}/**`, `../${m}/**`, `../../${m}/**`], message: 'Modules must not import each other.' })),
          { group: ['**/shell/**'], message: 'Modules must not import the application shell.' },
        ],
      },
    ],
  },
}));

export default tseslint.config(
  { ignores: ['dist'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...boundaryRules,
);
