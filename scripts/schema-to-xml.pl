#!/usr/bin/perl -w

use strict;


# **********************************************************************
# * OPENSCHEMA
# * An open source implementation of document structuring schemata.
# *
# * Copyright (C) 2004 Pablo Ariel Duboue
# * 
# * This library is free software; you can redistribute it and/or
# * modify it under the terms of the GNU Lesser General Public
# * License as published by the Free Software Foundation; either
# * version 2.1 of the License, or (at your option) any later version.
# *
# * This library is distributed in the hope that it will be useful,
# * but WITHOUT ANY WARRANTY; without even the implied warranty of
# * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# * GNU Lesser General Public License for more details.
# *
# * You should have received a copy of the GNU Lesser General Public
# * License along with this program; if not, write to the Free Software
# * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111, USA.
# ***********************************************************************



my$currentLevel=0;
my@rawText=<STDIN>;
chomp(@rawText);
my@text=();
my$longLine="";
foreach my$line(@rawText){
    $line=~s/\;.*$//;
    next if($line=~m/^\s*$/);
    if($longLine){
        if($line=~m/\\\\\s*$/){
            $line=~s/\\\\\s*$/ /;
            $longLine="$longLine$line";
        }else{
            $line="$longLine $line";
            push @text,$line unless($line=~m/^\s*\;/||!$line);
            $longLine="";
        }
    }else{
        push @text,$line unless($line=~m/^\s*\;/||!$line);
    }
}

print'<?xml version="1.0" encoding="iso-latin-1" standalone="yes"?>'."\n";
print'<OpenSchema '."\n";
print'  xmlns="http://openschema.sf.net"'."\n";
print'  xmlns:fd="http://jfuf.sf.net/FD">'."\n";

my$currentLine=0;
while($currentLine<=$#text){
    my$line=$text[$currentLine];
    my($spaces)=$line=~m/^(\s*)/;
    my$indent=$spaces?length($spaces):0;
    if($line=~m/^\s*predicate/){
        $currentLine=&parsePredicate($indent,$currentLine,\@text);
    }elsif($line=~m/^\s*schema/){ 
        $currentLine=&parseSchema($indent,$currentLine,\@text);
    }else{
        die "Syntax error line: $currentLine, '$line'\n";
    }
}
print '</OpenSchema>'."\n";

sub parsePredicate{
    my$indent=shift;
    my$currentLine=shift;
    my$text=shift;
    my$line=$$text[$currentLine];
    my($name)=$line=~m/predicate (\S+)\s*$/;
    print '<Predicate ID="'.$name.'">'."\n";
    $currentLine=&parseInsidePredicate($indent,$currentLine+1,$text);
    print '</Predicate>'."\n";
    return $currentLine;
}

sub parseInsidePredicate{
    my$thrIndent=shift;
    my$currentLine=shift;
    my$text=shift;
    my$line=$$text[$currentLine];
    my($spaces)=$line=~m/^(\s*)/;
    my$indent=$spaces?length($spaces):0;
    while($currentLine<=$#text&&$indent>$thrIndent){
        if($line=~m/\s*variables\s*/){
            $currentLine=&parseInsideVar($indent,$currentLine+1,$text);
        }elsif($line=~m/\s*properties\s*/){
            $currentLine=&parseInsideProp($indent,$currentLine+1,$text);
        }elsif($line=~m/\s*output\s*/){
            $currentLine=&parseInsideOutput($indent,$currentLine+1,$text);
        }else{
            die "Syntax error line: $currentLine, '$line'\n";
        }
        last if($currentLine>$#text);
        $line=$$text[$currentLine];
        ($spaces)=$line=~m/^(\s*)/;
        $indent=$spaces?length($spaces):0;
    }
    return$currentLine;
}

sub parseInsideVar{
    my$thrIndent=shift;
    my$currentLine=shift;
    my$text=shift;
    my$line=$$text[$currentLine];
    my($spaces)=$line=~m/^(\s*)/;
    my$indent=$spaces?length($spaces):0;
    while($currentLine<=$#text&&$indent>$thrIndent){
        my($req,$def,$name,$type)=$line=~m/^\s*(req\s+)?(def\s+)?([^\s]+)\s*\:\s*([^\s]+)\s*$/;
        #my$req=$parse[0] eq "req"?unshift @parse:0;
        #my$def=$parse[0] eq "def"?unshift @parse:0;
        #my($name,$type)=@parse;
        print '<Variable ID="'.$name.'" Type="'.$type.
            '" Required="'.($req?"true":"false").
            '" DefaultFocus="'.($def?"true":"false").'"/>'."\n";
        $currentLine++;
        last if($currentLine>$#text);
        $line=$$text[$currentLine];
        ($spaces)=$line=~m/^(\s*)/;
        $indent=$spaces?length($spaces):0;
    }
    return$currentLine;
}

sub parseInsideProp{
    my$thrIndent=shift;
    my$currentLine=shift;
    my$text=shift;
    my$line=$$text[$currentLine];
    my($spaces)=$line=~m/^(\s*)/;
    my$indent=$spaces?length($spaces):0;
    while($currentLine<=$#text&&$indent>$thrIndent){
        my$property=$line;
        $property=~s/^\s+//;$property=~s/\s+$//;
        print '<Property Value="'.$property.'"/>'."\n";
        $currentLine++;
        last if($currentLine>$#text);
        $line=$$text[$currentLine];
        ($spaces)=$line=~m/^(\s*)/;
        $indent=$spaces?length($spaces):0;
    }
    return$currentLine;
}

sub parseInsideOutput{
    my$thrIndent=shift;
    my$currentLine=shift;
    my$text=shift;
    my$line=$$text[$currentLine];
    my($spaces)=$line=~m/^(\s*)/;
    my$indent=$spaces?length($spaces):0;
    print'<Output>'."\n";
    print'<fd:FD>'."\n";
    my$currentIndent=$indent;
    my@indent=();
    while($currentLine<=$#text&&$indent>$thrIndent){
        my$output=$line;
        if($indent<$currentIndent){
            my$otherIndent=pop@indent;
            print'</fd:V>'."\n";
            while(@indent&&$indent<$otherIndent){
                $otherIndent=pop@indent;
                print'</fd:V>'."\n";
            }
        }
        $currentIndent=$indent;
        my($attr,$value)=$line=~m/^\s*([^\s]+)(\s+(.*))?$/;
        print'<fd:V N="'.$attr.'">';
        if($value){
            $value=~s/^\s+//;$value=~s/\s+$//;
            print'<fd:G>'.$value.'</fd:G></fd:V>'."\n";
        }else{
            print "\n";
            push@indent,$indent;
        }
        $currentIndent=$indent;

        $currentLine++;
        last if($currentLine>$#text);
        $line=$$text[$currentLine];
        ($spaces)=$line=~m/^(\s*)/;
        $indent=$spaces?length($spaces):0;
    }
    foreach(@indent){print '</fd:V>'."\n";}
    print'</fd:FD>'."\n";
    print'</Output>'."\n";
    return$currentLine;
}


sub parseSchema{
    my$indent=shift;
    my$currentLine=shift;
    my$text=shift;
    my$line=$$text[$currentLine];
    my($name)=$line=~m/predicate (\S+)\s*$/;
    print '<Schema>'."\n"; # ID="'.$name.'">'."\n";
    $currentLine=&parseInsideSchema($indent,$currentLine+1,$text);
    print '</Schema>'."\n";
    return $currentLine;
}

sub parseInsideSchema{
    my$thrIndent=shift;
    my$currentLine=shift;
    my$text=shift;
    my$line=$$text[$currentLine];
    my($spaces)=$line=~m/^(\s*)/;
    my$indent=$spaces?length($spaces):0;
    my$currentIndent=$indent;
    my@indent=();
    my@open=();
    while($currentLine<=$#text&&$indent>$thrIndent){
        my$output=$line;
        if($indent<$currentIndent){
            #print STDERR "$indent $currentIndent\n";
            if(!@open){
                foreach my$l($currentLine-5..$currentLine+5){
                    print STDERR ($l==$currentLine?"=> ":"   ").$$text[$l]."\n";
                }
                die"\@open is empty\n";
            }
            my$otherIndent=pop@indent;
            my$operator=pop@open;
            print'</'.$operator.'></Node>'."\n";
            #print STDERR "\t$otherIndent $indent\n";
            while(@indent&&$indent<$otherIndent){
                $otherIndent=pop@indent;
                $operator=pop@open;
                print'</'.$operator.'></Node>'."\n";
                #print STDERR "\t\t$otherIndent $currentIndent\n";
            }
        }
        $currentIndent=$indent;
        if($line=~m/\s*aggregation-boundary/){
            print'<Node><AggrBoundary/></Node>'."\n";
        }elsif($line=~m/\s*paragraph-boundary/){
            print'<Node><ParBoundary/></Node>'."\n";
        }elsif($line=~m/\s*(choice)|(sequence)|(star)|(plus)|(optional)/){
            my$operator="Sequence";
            if($line=~m/\s*choice/){
                $operator="Choice";
            }elsif($line=~m/\s*sequence/){
                $operator="Sequence";
            }elsif($line=~m/\s*optional/){
                $operator="Optional";
            }elsif($line=~m/\s*star/){
                $operator="KleeneStar";
            }elsif($line=~m/\s*plus/){
                $operator="KleenePlus";
            }
            print'<Node>';
            print'<'.$operator.'>'."\n";
            push@open,$operator;
            push@indent,$indent;
        }else{
            my($predicate,$variableBindings)=$line=~m/^\s*(.*)\((.*)\)\s*$/;
            die "'$line'\n"unless($predicate);
            my@pairs=();
            if($variableBindings){
                my@varBindPairs=split(/,/,$variableBindings);
                @pairs=map{my@varBinding=split(/\|/,$_);[ @varBinding ]}@varBindPairs;
            }
            print'<Node><Predicate Name="'.$predicate.'">'."\n";
            foreach my$pair(@pairs){
                print'<Variable Name="'.$$pair[0].'" Value="'.$$pair[1].'"/>'."\n";
            }
            print'</Predicate></Node>'."\n";
        }
        $currentIndent=$indent;

        $currentLine++;
        last if($currentLine>$#text);
        $line=$$text[$currentLine];
        ($spaces)=$line=~m/^(\s*)/;
        $indent=$spaces?length($spaces):0;
    }
    foreach my$operator(reverse @open){print '</'.$operator.'></Node>'."\n";}
    return$currentLine;
}
