import {
  Component, Input, Output, EventEmitter,
  AfterViewInit, OnDestroy, OnChanges, SimpleChanges,
  ViewChild, ElementRef, NgZone, inject,
  ChangeDetectionStrategy, signal, ViewEncapsulation
} from '@angular/core';
import { Editor } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';

interface ToolbarState {
  bold: boolean; italic: boolean; underline: boolean;
  h1: boolean; h2: boolean;
  bullet: boolean; ordered: boolean;
  blockquote: boolean; code: boolean;
}

const EMPTY: ToolbarState = {
  bold: false, italic: false, underline: false,
  h1: false, h2: false, bullet: false, ordered: false,
  blockquote: false, code: false,
};

const TIPTAP_EMPTY = '<p></p>';

@Component({
  selector: 'app-rich-text-editor',
  standalone: true,
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="rte-wrapper">
      <div class="rte-toolbar">
        <button type="button" class="rte-btn rte-bold"       [class.active]="s().bold"      (click)="cmd('bold')"         title="Bold">B</button>
        <button type="button" class="rte-btn rte-italic"     [class.active]="s().italic"    (click)="cmd('italic')"       title="Italic">I</button>
        <button type="button" class="rte-btn rte-underline"  [class.active]="s().underline" (click)="cmd('underline')"    title="Underline">U</button>
        <div class="rte-sep"></div>
        <button type="button" class="rte-btn"                [class.active]="s().h1"        (click)="heading(1)"          title="Heading 1">H1</button>
        <button type="button" class="rte-btn"                [class.active]="s().h2"        (click)="heading(2)"          title="Heading 2">H2</button>
        <div class="rte-sep"></div>
        <button type="button" class="rte-btn"                [class.active]="s().bullet"    (click)="cmd('bulletList')"   title="Bullet list">•</button>
        <button type="button" class="rte-btn"                [class.active]="s().ordered"   (click)="cmd('orderedList')"  title="Ordered list">1.</button>
        <div class="rte-sep"></div>
        <button type="button" class="rte-btn"                [class.active]="s().blockquote"(click)="cmd('blockquote')"   title="Quote">"</button>
        <button type="button" class="rte-btn rte-mono"       [class.active]="s().code"      (click)="cmd('codeBlock')"    title="Code">&lt;/&gt;</button>
      </div>
      <div #editorEl class="rte-body"></div>
    </div>
  `,
  styles: [`
    .rte-wrapper {
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      overflow: hidden;
      background: white;
    }

    .rte-toolbar {
      display: flex;
      align-items: center;
      gap: 2px;
      padding: 5px 8px;
      background: #f8fafc;
      border-bottom: 1px solid #e2e8f0;
      flex-wrap: wrap;
    }

    .rte-btn {
      height: 24px;
      min-width: 24px;
      padding: 0 5px;
      border: 1px solid transparent;
      border-radius: 4px;
      background: none;
      cursor: pointer;
      font-size: 12px;
      font-weight: 700;
      color: #475569;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: inherit;
      line-height: 1;
    }

    .rte-btn:hover { background: #e2e8f0; }
    .rte-btn.active { background: #ede9fe; color: #6366f1; border-color: #c4b5fd; }
    .rte-bold { font-weight: 900; }
    .rte-italic { font-style: italic; font-weight: 700; }
    .rte-underline { text-decoration: underline; }
    .rte-mono { font-family: monospace; font-size: 11px; }

    .rte-sep {
      width: 1px;
      height: 16px;
      background: #e2e8f0;
      margin: 0 3px;
      flex-shrink: 0;
    }

    .rte-body {
      min-height: 90px;
      max-height: 320px;
      overflow-y: auto;
      padding: 10px 12px;
      cursor: text;
    }

    /* ProseMirror content styles */
    .rte-body .ProseMirror { outline: none; font-size: 13px; color: #1e293b; line-height: 1.65; }
    .rte-body .ProseMirror > * + * { margin-top: 6px; }
    .rte-body .ProseMirror p { margin: 0; }
    .rte-body .ProseMirror h1 { font-size: 17px; font-weight: 700; color: #0f172a; }
    .rte-body .ProseMirror h2 { font-size: 14px; font-weight: 700; color: #0f172a; }
    .rte-body .ProseMirror ul, .rte-body .ProseMirror ol { padding-left: 20px; }
    .rte-body .ProseMirror li > p { margin: 0; }
    .rte-body .ProseMirror blockquote { border-left: 3px solid #6366f1; padding-left: 10px; color: #64748b; font-style: italic; }
    .rte-body .ProseMirror code { background: #f1f5f9; border-radius: 3px; padding: 1px 4px; font-family: monospace; font-size: 12px; }
    .rte-body .ProseMirror pre { background: #1e293b; color: #e2e8f0; border-radius: 6px; padding: 10px 12px; overflow-x: auto; }
    .rte-body .ProseMirror pre code { background: none; padding: 0; font-size: 12px; color: inherit; }
    .rte-body .ProseMirror a { color: #6366f1; text-decoration: underline; }
    .rte-body .ProseMirror p.is-editor-empty:first-child::before {
      content: attr(data-placeholder);
      float: left;
      color: #94a3b8;
      pointer-events: none;
      height: 0;
    }
  `]
})
export class RichTextEditorComponent implements AfterViewInit, OnChanges, OnDestroy {

  @Input() content = '';
  @Input() placeholder = 'Write a description...';
  @Output() contentChange = new EventEmitter<string>();

  @ViewChild('editorEl') private editorEl!: ElementRef<HTMLDivElement>;

  private readonly ngZone = inject(NgZone);
  private editor?: Editor;

  readonly s = signal<ToolbarState>({ ...EMPTY });

  ngAfterViewInit(): void {
    this.editor = new Editor({
      element: this.editorEl.nativeElement,
      extensions: [
        StarterKit,
        Underline,
        Link.configure({ openOnClick: false }),
        Placeholder.configure({ placeholder: this.placeholder }),
      ],
      content: this.content || '',
      onUpdate: ({ editor }) => {
        const html = editor.getHTML();
        this.ngZone.run(() => {
          this.contentChange.emit(html === TIPTAP_EMPTY ? '' : html);
          this.sync(editor);
        });
      },
      onSelectionUpdate: ({ editor }) => {
        this.ngZone.run(() => this.sync(editor));
      },
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['content'] && this.editor) {
      const incoming = changes['content'].currentValue ?? '';
      const current = this.editor.getHTML();
      const currentNorm = current === TIPTAP_EMPTY ? '' : current;
      if (incoming !== currentNorm) {
        this.editor.commands.setContent(incoming || '', false);
      }
    }
  }

  ngOnDestroy(): void {
    this.editor?.destroy();
  }

  cmd(type: string): void {
    if (!this.editor) return;
    const c = this.editor.chain().focus();
    switch (type) {
      case 'bold':        c.toggleBold().run(); break;
      case 'italic':      c.toggleItalic().run(); break;
      case 'underline':   c.toggleUnderline().run(); break;
      case 'bulletList':  c.toggleBulletList().run(); break;
      case 'orderedList': c.toggleOrderedList().run(); break;
      case 'blockquote':  c.toggleBlockquote().run(); break;
      case 'codeBlock':   c.toggleCodeBlock().run(); break;
    }
  }

  heading(level: 1 | 2 | 3): void {
    this.editor?.chain().focus().toggleHeading({ level }).run();
  }

  private sync(editor: Editor): void {
    this.s.set({
      bold:       editor.isActive('bold'),
      italic:     editor.isActive('italic'),
      underline:  editor.isActive('underline'),
      h1:         editor.isActive('heading', { level: 1 }),
      h2:         editor.isActive('heading', { level: 2 }),
      bullet:     editor.isActive('bulletList'),
      ordered:    editor.isActive('orderedList'),
      blockquote: editor.isActive('blockquote'),
      code:       editor.isActive('codeBlock'),
    });
  }
}
