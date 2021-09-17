export interface ITodo {
  id?: number;
  subject?: string;
  detail?: string | null;
}

export class Todo implements ITodo {
  constructor(public id?: number, public subject?: string, public detail?: string | null) {}
}

export function getTodoIdentifier(todo: ITodo): number | undefined {
  return todo.id;
}
