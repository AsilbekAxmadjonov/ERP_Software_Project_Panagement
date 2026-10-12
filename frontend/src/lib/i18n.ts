const dictionary: Record<string, string> = {};

export const __ = (text: string): string => dictionary[text] ?? text;
